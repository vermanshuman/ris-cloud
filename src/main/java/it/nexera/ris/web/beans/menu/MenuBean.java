package it.nexera.ris.web.beans.menu;

import com.sun.faces.facelets.tag.ui.ComponentRef;
import it.nexera.ris.common.enums.MenuItemTypes;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Module;
import it.nexera.ris.persistence.beans.entities.domain.ModulePage;
import it.nexera.ris.web.beans.PageBean;
import it.nexera.ris.web.beans.base.AccessBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.primefaces.component.graphicimage.GraphicImage;
import org.primefaces.component.panel.Panel;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import java.io.Serializable;

@Named("menuBean")
@ViewScoped
public class MenuBean extends PageBean implements Serializable {

    private static final long serialVersionUID = -5426864162684772597L;

    private Integer activeIndex;

    private Boolean renderSubMenu;

    private MenuModel model;

    private boolean flag;

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.PageBean#onConstruct()
     */
    @Override
    protected void onConstruct() {
        if (this.getCurrentPage() != null
                && this.getCurrentPage().isDictionaryPage()) {
            model = new DefaultMenuModel();

            DefaultSubMenu dictionaryHeader = DefaultSubMenu.builder()
                    .label(ResourcesHelper.getString("dictionaryMenuHeader"))
                    .build();
            model.getElements().add(dictionaryHeader);
            model.getElements().add(createFirstSubmenu());
            model.getElements().add(createSecondSubmenu());
            model.getElements().add(createThirdSubmenu());
        }
        if (!isPostback()) {
            setFlag(false);
        }
    }

    private DefaultSubMenu createFirstSubmenu() {
        DefaultSubMenu submenu = DefaultSubMenu.builder()
                .label(ResourcesHelper.getString("dictionarySubGroup1"))
                .styleClass("sub_menu_sub_group")
                .build();
        try {
            submenuAddElement(submenu,
                    DaoManager.get(Module.class, new Criterion[]
                            {Restrictions.eq("code", "PDS")}), PageTypes.SECTOR_LIST);
            submenuAddElement(submenu,
                    DaoManager.get(Module.class, new Criterion[]
                            {Restrictions.eq("code", "PDH")}),
                    PageTypes.HOSPITAL_LIST);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return submenu;
    }

    private DefaultSubMenu createSecondSubmenu() {
        DefaultSubMenu submenu = DefaultSubMenu.builder()
                .label(ResourcesHelper.getString("dictionarySubGroup2"))
                .styleClass("sub_menu_sub_group")
                .build();

        try {
            submenuAddElement(submenu,
                    DaoManager.get(Module.class, new Criterion[]
                            {Restrictions.eq("code", "PDRE")}),
                    PageTypes.RADIOLOGY_EXAM_LIST);
            submenuAddElement(submenu,
                    DaoManager.get(Module.class, new Criterion[]
                            {Restrictions.eq("code", "PDET")}),
                    PageTypes.EXAM_TYPE_LIST);
            submenuAddElement(submenu,
                    DaoManager.get(Module.class, new Criterion[]
                            {Restrictions.eq("code", "PDD")}),
                    PageTypes.DIAGNOSTIC_LIST);
            submenuAddElement(submenu,
                    DaoManager.get(Module.class, new Criterion[]
                            {Restrictions.eq("code", "PDP")}), PageTypes.PACKAGE_LIST);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return submenu;
    }

    private DefaultSubMenu createThirdSubmenu() {

        DefaultSubMenu submenu = DefaultSubMenu.builder()
                .label(ResourcesHelper.getString("dictionarySubGroup3"))
                .styleClass("sub_menu_sub_group")
                .build();

        try {
            /*
             * for CR 0028862
             * 
             * submenuAddElement(submenu, ModuleTypes.PRIORITY_LIST,
             * PageTypes.PRIORITY_LIST);
             */
            submenuAddElement(submenu,
                    DaoManager.get(Module.class, new Criterion[]
                            {Restrictions.eq("code", "PDU")}), PageTypes.URGENCY_LIST);
            submenuAddElement(submenu,
                    DaoManager.get(Module.class, new Criterion[]
                            {Restrictions.eq("code", "PDTDM")}),
                    PageTypes.TEMPLATE_DOCUMENT_MODEL_LIST);
            submenuAddElement(submenu,
                    DaoManager.get(Module.class, new Criterion[]
                            {Restrictions.eq("code", "PDO")}), PageTypes.OSIRIX_LIST);
            submenuAddElement(submenu,
                    DaoManager.get(Module.class, new Criterion[]{
                            Restrictions.eq("code", "PDDP")
                    }), PageTypes.DVD_PRODUCER_LIST);
            submenuAddElement(submenu,
                    DaoManager.get(Module.class, new Criterion[]
                            {Restrictions.eq("code", "PDPACS")}), PageTypes.PACS_LIST);
            submenuAddElement(submenu,
                    DaoManager.get(Module.class, new Criterion[]
                            {Restrictions.eq("code", "PDDR")}),
                    PageTypes.DIRECT_RESERVATION_LIST);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return submenu;
    }

    private void submenuAddElement(DefaultSubMenu submenu, Module module,
                                   PageTypes pageType) throws PersistenceBeanException,
            HibernateException, InstantiationException, IllegalAccessException {
        if (AccessBean.canAccessPage(pageType)) {
            submenu.getElements().add(createSubmenuItem(module, pageType));
        }
    }

    private DefaultMenuItem createSubmenuItem(Module module, PageTypes pageTypes)
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        DefaultMenuItem item = DefaultMenuItem.builder()
                .value(module.toString())
                .build();
        item.setIcon("ui-icon-note");
        item.setUrl(getUrlWithContextPrepended(pageTypes));
        String pageName = DaoManager.get(ModulePage.class, new Criterion[]
                {Restrictions.eq("module.id", module.getId())}).getPage_type();
        if (this.getCurrentPage().toString().equals(pageName)) {
            item.setStyleClass("ui-state-active");
        } else {
            if (this.getCurrentPage().getCode().contains("EDIT")) {
                String p = this.getCurrentPage().getCode()
                        .replace("EDIT", "LIST");
                if (p.equals(pageName.toString())) {
                    item.setStyleClass("ui-state-active");
                }
            }
        }
        return item;
    }

    private static String getUrlWithContextPrepended(PageTypes pageType) {
        String url = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestContextPath();
        if (!pageType.getPagesContext().startsWith("/")) {
            url = url + "/";
        }
        return url + pageType.getPagesContext();
    }

    public MenuModel getModel() {
        if (this.getCurrentPage() != null
                && !this.getCurrentPage().isDictionaryPage()) {
            return null;
        }

        return model;
    }

    public void gotoUserEdit() {
        RedirectHelper.goTo(PageTypes.USER_EDIT);
    }

    public void gotoLogin() {
        RedirectHelper.goTo(PageTypes.LOGIN);
    }

    public void gotoUserList() {
        RedirectHelper.goTo(PageTypes.USER_LIST);
    }

    public void gotoRoleEdit() {
        RedirectHelper.goTo(PageTypes.ROLE_EDIT);
    }

    public void gotoRoleList() {
        RedirectHelper.goTo(PageTypes.ROLE_LIST);
    }

    public ComponentRef getMainMenu() {
        try {
            return getMainMenuInternal();
        } catch (Exception e) {
            try {
                return getMainMenuInternal();
            } catch (Exception e1) {
                LogHelper.log(log, e1);
            }
        }
        return new ComponentRef();
    }

    public void setMainMenu(ComponentRef componentRef) {
    }

    private ComponentRef getMainMenuInternal() throws PersistenceBeanException,
            HibernateException, InstantiationException, IllegalAccessException {
        ComponentRef comp = new ComponentRef();

        HtmlPanelGroup mainMenu = this.createMainMenuPanelGroup("main_menu");

        log.info("Starting to generate MenuItem blocks");
        long startTime = ExecutionTimeHelper.getStartExecutionTime();

        generateMenuItemsBlocks(mainMenu, "menuConfiguration",
                PageTypes.USER_LIST, PageTypes.ROLE_LIST,
                PageTypes.APPLICATION_SETTINGS, PageTypes.MONITORING_VIEW,
                PageTypes.EVENT_CALENDAR_LIST,
                PageTypes.DOCUMENT_TEMPLATE_LIST,
                PageTypes.INSTANCE_PHASES_LIST);

        generateMenuItemsBlocks(mainMenu, "menuConfigurationArea",
                PageTypes.DICTIONARY_LIST, PageTypes.PATIENT_LIST);

        long executionTime = ExecutionTimeHelper.getExecutionTime(startTime);
        log.info("Finished generating MenuItem blocks. Elapsed time (ns): "
                .concat(String.valueOf(executionTime)));

        /*
         * generateMenuItemsBlocks(mainMenu, "menuManagementGroup",
         * PageTypes.PATIENT_SEARCH, PageTypes.WAITINGLIST_REGISTRATION_LIST,
         * PageTypes.WORKLIST, PageTypes.DATE_CONFIRMATION_VIEW);
         */

        mainMenu.setId("mainMenuWrapper");

        comp.getChildren().add(mainMenu);
        return comp;
    }

    public void generateMenuItemsBlocks(HtmlPanelGroup mainMenu,
                                        String sectionName, PageTypes... pages) throws HibernateException,
            PersistenceBeanException, IllegalAccessException,
            InstantiationException {
        int menuSubItems = 0;
        HtmlPanelGroup menuGroup = this
                .createMenuPanelGroup(this.getUniqueId());
        Panel section = this.createSection(sectionName);
        HtmlPanelGroup subMainMenu = this.createMainMenuPanelGroup(this
                .getUniqueId());

        for (PageTypes page : pages) {
            for (MenuItemTypes menuItemType : MenuItemTypes.values()) {
                if (page.getCode().toLowerCase().contains(menuItemType.name().toLowerCase())) {
                    menuSubItems += createMenuItem(subMainMenu, page,
                            menuItemType);
                }
            }
        }
        addMenuSubItems(mainMenu, menuGroup, section, subMainMenu, menuSubItems);
    }

    public void addMenuSubItems(HtmlPanelGroup mainMenu,
                                HtmlPanelGroup menuGroup, Panel section,
                                HtmlPanelGroup subMainMenu, int menuSubItems) {
        if (menuSubItems > 0) {
            section.getChildren().add(subMainMenu);
            menuGroup.getChildren().add(section);
            mainMenu.getChildren().add(menuGroup);
        }
    }

    public Panel createSection(String menuLabel) {
        Panel section;
        section = new Panel();
        section.setId(menuLabel);
        section.setHeader(ResourcesHelper.getString(menuLabel));
        section.setToggleable(true);
        Cookie cook = CookieHelper.getCookie(menuLabel);
        section.setCollapsed(cook != null && Boolean.parseBoolean(cook.getValue()));
        section.setToggleSpeed(300);
        return section;
    }

    private HtmlPanelGroup createMainMenuPanelGroup(String id) {
        HtmlPanelGroup menuGroup = this.createPanelGroup(id);
        menuGroup.setStyleClass("main_menu");
        return menuGroup;
    }

    private HtmlPanelGroup createMenuPanelGroup(String id) {
        HtmlPanelGroup menuGroup = this.createPanelGroup(id);
        menuGroup.setStyleClass("menu_group");
        return menuGroup;
    }

    private HtmlPanelGroup createPanelGroup(String id) {
        HtmlPanelGroup htmlPanelGroup = new HtmlPanelGroup();
        htmlPanelGroup.setId(id);
        htmlPanelGroup.setLayout("block");
        return htmlPanelGroup;
    }

    private int createMenuItem(HtmlPanelGroup subMainMenu, PageTypes pageType,
                               MenuItemTypes itemType) throws HibernateException,
            PersistenceBeanException, IllegalAccessException,
            InstantiationException {
        if (AccessBean.canViewPage(pageType)) {
            HtmlPanelGroup subMenuItem = new HtmlPanelGroup();
            subMenuItem.setId(itemType.getId());
            subMenuItem.setLayout("block");
            if (!PageTypes.DICTIONARY_LIST.equals(pageType)) {
                if (this.getCurrentPage() != null
                        && this.getCurrentPage().equals(pageType)) {
                    subMenuItem
                            .setStyleClass("draggable ui-state-active ui-corner-all");
                } else if (this.getCurrentPage() != null
                        && this.getCurrentPage().getCode().contains("EDIT")) {
                    String p = this.getCurrentPage().getCode()
                            .replace("EDIT", "LIST");
                    if (p.equals(pageType.toString())) {
                        subMenuItem
                                .setStyleClass("draggable ui-state-active ui-corner-all");
                    }
                } else {
                    subMenuItem.setStyleClass("draggable ui-corner-all");
                }
            } else {
                if (this.getCurrentPage() != null
                        && (this.getCurrentPage().equals(pageType) || this
                        .getCurrentPage().isDictionaryPage())) {
                    subMenuItem
                            .setStyleClass("draggable ui-state-active ui-corner-all");
                    if (!isFlag()) {
                        executeJS("updateMenuItem();onPageLoadGetMenuItem();");
                        setFlag(true);
                    }
                } else {
                    subMenuItem.setStyleClass("draggable ui-corner-all");
                }
            }

            HtmlOutputLink menuLink = new HtmlOutputLink();
            menuLink.setId(this.getUniqueId());
            menuLink.setValue(String.format("%s%s", this.getContext()
                            .getExternalContext().getRequestContextPath(),
                    pageType.getPagesContext()));

            GraphicImage image = new GraphicImage();
            image.setValue("/resources/images/menu/" + itemType.getImage());

            menuLink.getChildren().add(image);
            HtmlPanelGroup menuTitle = new HtmlPanelGroup();
            menuTitle.setId(this.getUniqueId());
            menuTitle.setStyleClass("name");

            HtmlOutputText text = new HtmlOutputText();
            text.setId(this.getUniqueId());
            text.setValue(itemType.toString());

            menuTitle.getChildren().add(text);
            menuLink.getChildren().add(menuTitle);
            subMenuItem.getChildren().add(menuLink);
            subMainMenu.getChildren().add(subMenuItem);
            return 1;
        } else {
            return 0;
        }
    }

    public void setActiveIndex(Integer activeIndex) {
        this.activeIndex = activeIndex;
    }

    public Integer getActiveIndex() {
        return activeIndex;
    }

    public Boolean getRenderSubMenu() {
        return renderSubMenu;
    }

    public void setRenderSubMenu(Boolean renderSubMenu) {
        this.renderSubMenu = renderSubMenu;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

}
