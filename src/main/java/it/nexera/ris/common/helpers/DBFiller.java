package it.nexera.ris.common.helpers;

import it.nexera.ris.common.annotations.View;
import it.nexera.ris.common.enums.EnableDisableEnum;
import it.nexera.ris.common.enums.RoleTypes;
import it.nexera.ris.common.enums.UserStatuses;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.security.crypto.MD5;
import it.nexera.ris.common.xml.wrappers.*;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.DatabaseIndex;
import it.nexera.ris.persistence.beans.entities.DatabaseMaterializedView;
import it.nexera.ris.persistence.beans.entities.DatabaseTrigger;
import it.nexera.ris.persistence.beans.entities.IndexedView;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.Module;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.SessionImpl;
import org.hibernate.sql.JoinType;
import org.reflections.Reflections;

import javax.persistence.PersistenceException;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class DBFiller extends BaseHelper {
    // need fill

    public static boolean needFillEntity(Session session, Class<? extends it.nexera.ris.persistence.beans.entities.Entity> entity)
            throws PersistenceBeanException, IllegalAccessException {
        return ConnectionManager.getCount(entity, session) == 0l
                || entity == ModulePage.class || entity == Module.class || entity == Permission.class;
    }

    public static boolean needFillUsers(Session session) {
        return ConnectionManager.load(User.class, new CriteriaAlias[]{
                new CriteriaAlias("roles", "role", JoinType.LEFT_OUTER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("role.type", RoleTypes.ADMINISTRATOR)
        }, session).isEmpty();
    }

    // fill

    public static List<Role> fillRoles() {
        List<Role> list = new ArrayList<Role>();

        Role role = new Role();
        role.setType(RoleTypes.ADMINISTRATOR);
        role.setName("Administrator");
        list.add(role);

        return list;
    }

    public static List<Permission> fillPermissions(File importFile,
                                                   Session session) {
        List<Permission> permissions = null;

        try {
            PermissionList permissionList = new PermissionList();

            JAXBContext context = JAXBContext.newInstance(PermissionList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            permissionList = (PermissionList) um.unmarshal(importFile);

            for (Permission permission : permissionList.getPermissions()) {
                Module module = null;
                try {
                    module = ConnectionManager.get(
                            Module.class,
                            new Criterion[]{
                                    Restrictions.eq("code",
                                            permission.getModule_code())
                            }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
                Role role = null;
                try {
                    role = ConnectionManager.get(Role.class, new Criterion[]{
                            Restrictions.eq("id", permission.getRole_id())
                    }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
                permission.setModule(module);
                permission.setRole(role);
            }

            permissions = permissionList.getPermissions();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return permissions;
    }

    public static void fillSequences(Session session) {
        try {
            Reflections reflections = new Reflections(
                    "it.nexera.ris.persistence.beans.entities.domain");

            Set<Method> methods = reflections
                    .getMethodsAnnotatedWith(Deprecated.class);
            System.out.println(methods);

            Set<Class<?>> annotated = reflections
                    .getTypesAnnotatedWith((Class<? extends Annotation>) javax.persistence.Entity.class);
            for (Class<?> clazz : annotated) {
                SequenceGenerator annotation = clazz.getAnnotation(SequenceGenerator.class);
                if (annotation == null) continue;

                String seqName = annotation.sequenceName();
                BigDecimal exist = (BigDecimal) session.createSQLQuery(
                        "select count(*) from user_sequences where sequence_name = '"
                                + seqName + "'").uniqueResult();

                if (exist == null || exist.compareTo(BigDecimal.ONE) == -1) {
                    session.createSQLQuery(
                            "CREATE SEQUENCE \""
                                    + HibernateUtil.getUsername()
                                    + "\".\""
                                    + seqName
                                    + "\" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 NOCACHE NOORDER NOCYCLE ")
                            .executeUpdate();
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void createViews(Session session) {
        try {
            Reflections reflections = new Reflections(
                    "it.nexera.ris.persistence.view");

            Set<Class<?>> annotated = reflections
                    .getTypesAnnotatedWith((Class<? extends Annotation>) javax.persistence.Entity.class);
            for (Class<?> clazz : annotated) {
                String sql = ((View) clazz.getAnnotation(View.class)).sql();
                session.createSQLQuery(sql).executeUpdate();
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void createTriggers(Session session) {
        try {
            for (String triggerSQL : DatabaseTrigger.getTriggers()) {
                Connection conn = ((SessionImpl) session).connection();
                try (Statement state = conn.createStatement()) {
                    state.execute(triggerSQL);
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void createIndexes(Session session) {
        try {
            session.createSQLQuery(DatabaseIndex.ALLOW_WAIT_FOR_DDL).executeUpdate();

            for (String indexSQL : DatabaseIndex.getIndexes()) {
                Connection conn = ((SessionImpl) session).connection();
                try (Statement state = conn.createStatement()) {
                    state.execute(indexSQL);
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void updateEmailTypes(Session session) {
        for(EmailTemplate.EmailTypes t : EmailTemplate.EmailTypes.values()){
            updateEmailType(t, session);
        }
    }

    private static void updateEmailType(EmailTemplate.EmailTypes type, Session session) {
        try {
            EmailTemplate emailTemplate = ConnectionManager.get(EmailTemplate.class, new Criterion[]{
                    Restrictions.eq("emailType", type)
            }, session);
            if (emailTemplate == null) {
                emailTemplate = new EmailTemplate();
                emailTemplate.setEmailType(type);
                ConnectionManager.save(emailTemplate, session);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void createMViews(Session session) {
        try {
            Reflections reflections = new Reflections("it.nexera.ris.persistence.materialized");

            Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(javax.persistence.Entity.class);
            for (Class<?> clazz : annotated) {
                if (IndexedView.class.isAssignableFrom(clazz)) {
                    try {
                        String tableName = clazz.getAnnotation(Table.class).name();
                        try {
                            session.createSQLQuery("DROP TABLE " + tableName).executeUpdate();
                        } catch (Exception e) {
                            try {
                                session.createSQLQuery("DROP MATERIALIZED VIEW " + tableName).executeUpdate();
                            } catch (Exception ie) {
                                LogHelper.log(log, ie);
                            }
                        }
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }
                }
            }
            for (String mViewSQL : DatabaseMaterializedView.getMViews()) {
                Connection conn = ((SessionImpl) session).connection();
                try (Statement state = conn.createStatement()) {
                    state.execute(mViewSQL);
                } catch (SQLException e) {
                    log.warn(e);
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static List<User> fillUsers(Session session)
            throws HibernateException, PersistenceException,
            InstantiationException, IllegalAccessException {
        List<User> list = new ArrayList<User>();

        User user = new User();
        user = new User();
        user.setFirstName("admin");
        user.setLastName("admin");
        user.setEmail("admin@ris.it");
        user.setLogin("admin");
        user.setPassword(MD5.encodeString("11111111", null));
        user.setNotDeletable(Boolean.TRUE);
        user.setStatus(UserStatuses.ACTIVE);

        user.setRoles(new ArrayList<Role>());
        user.getRoles().add(
                ConnectionManager.get(Role.class,
                        Restrictions.eq("type", RoleTypes.ADMINISTRATOR),
                        session));

        list.add(user);
        return list;
    }

    // from xml

    public static List<RadiologyExam> fillRadiologies(File importFile,
                                                      Session session) {
        List<RadiologyExam> exams = null;

        try {
            RadiologyExamList radiologyExamList = new RadiologyExamList();

            JAXBContext context = JAXBContext
                    .newInstance(RadiologyExamList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            radiologyExamList = (RadiologyExamList) um.unmarshal(importFile);

            exams = radiologyExamList.getRadExams();
            for (RadiologyExam radiologyExam : exams) {
                radiologyExam.setState(EnableDisableEnum.ENABLE);
            }
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return exams;
    }

    public static List<Asl> fillAsls(File importFile, Session session) {
        List<Asl> asls = null;

        try {
            AslList aslList = new AslList();

            JAXBContext context = JAXBContext.newInstance(AslList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            aslList = (AslList) um.unmarshal(importFile);

            asls = aslList.getAsls();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return asls;
    }

    public static List<ModulePage> fillModulePages(File importFile,
                                                   Session session) {
        List<ModulePage> modulePages = null;

        try {
            ModulePageList modulePageList = new ModulePageList();

            JAXBContext context = JAXBContext.newInstance(ModulePageList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            modulePageList = (ModulePageList) um.unmarshal(importFile);

            for (ModulePage modulePageItem : modulePageList.getModulePages()) {

                Module module = null;
                try {
                    module = ConnectionManager.get(
                            Module.class,
                            new Criterion[]{
                                    Restrictions.eq("code",
                                            modulePageItem.getModule_code())
                            }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
                modulePageItem.setModule(module);
            }

            modulePages = modulePageList.getModulePages();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return modulePages;
    }

    public static List<Module> fillModuleNonEnums(File importFile, Session session) {
        List<Module> modules = null;

        try {
            JAXBContext context = JAXBContext.newInstance(ModuleList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            ModuleList moduleList = (ModuleList) um.unmarshal(importFile);

            modules = moduleList.getModules();

            for (Module module : modules) {
                if (module.getParent() != null && module.getParent().getCode() != null) {
                    Module persistModule = ConnectionManager.get(Module.class, new Criterion[]{
                            Restrictions.eq("code", module.getParent().getCode())
                    }, session);
                    if (persistModule != null) {
                        module.setParent(persistModule);
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return modules;
    }

    public static List<AslRegion> fillAslRegions(File importFile,
                                                 Session session) {
        List<AslRegion> aslRegions = null;

        try {
            AslRegionList aslRegionList = new AslRegionList();

            JAXBContext context = JAXBContext.newInstance(AslRegionList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            aslRegionList = (AslRegionList) um.unmarshal(importFile);

            aslRegions = aslRegionList.getAslRegions();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return aslRegions;
    }

    public static List<City> fillCities(File importFile, Session session) {
        List<City> cities = null;

        try {
            CityList cityList = new CityList();

            JAXBContext context = JAXBContext.newInstance(CityList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            cityList = (CityList) um.unmarshal(importFile);

            for (City city : cityList.getCities()) {
                Asl asl = null;
                try {
                    asl = ConnectionManager.get(Asl.class, new Criterion[]{
                            Restrictions.eq("code", city.getAsl_code())
                    }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }

                AslRegion aslRegion = null;
                try {
                    aslRegion = ConnectionManager.get(
                            AslRegion.class,
                            new Criterion[]{
                                    Restrictions.eq("code",
                                            city.getAsl_region_code())
                            }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }

                Province province = null;
                try {
                    province = ConnectionManager
                            .get(Province.class,
                                    new Criterion[]{
                                            Restrictions.eq("code",
                                                    city.getProvince_code())
                                    }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
                city.setAsl(asl);
                city.setAslRegion(aslRegion);
                city.setProvince(province);
            }

            cities = cityList.getCities();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return cities;
    }

    public static List<Nationality> fillNationalities(File importFile,
                                                      Session session) {
        List<Nationality> nationalities = null;

        try {
            NationalityList nationalityList = new NationalityList();

            JAXBContext context = JAXBContext
                    .newInstance(NationalityList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            nationalityList = (NationalityList) um.unmarshal(importFile);

            for (Nationality nationality : nationalityList.getNationalities()) {
                AslRegion aslRegion = null;
                try {
                    aslRegion = ConnectionManager.get(
                            AslRegion.class,
                            new Criterion[]{
                                    Restrictions.eq("code",
                                            nationality.getAsl_region_code())
                            }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }

                nationality.setAslRegion(aslRegion);
            }

            nationalities = nationalityList.getNationalities();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return nationalities;
    }

    public static List<Province> fillProvinces(File importFile, Session session) {
        List<Province> provinces = null;

        try {
            ProvinceList nationalityList = new ProvinceList();

            JAXBContext context = JAXBContext.newInstance(ProvinceList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            nationalityList = (ProvinceList) um.unmarshal(importFile);

            provinces = nationalityList.getProvinces();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return provinces;
    }
}

