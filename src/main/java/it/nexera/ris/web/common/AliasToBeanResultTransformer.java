package it.nexera.ris.web.common;

import org.hibernate.HibernateException;
import org.hibernate.property.access.internal.PropertyAccessStrategyBasicImpl;
import org.hibernate.property.access.internal.PropertyAccessStrategyChainedImpl;
import org.hibernate.property.access.internal.PropertyAccessStrategyFieldImpl;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.transform.AliasedTupleSubsetResultTransformer;

import java.util.Arrays;

public class AliasToBeanResultTransformer extends AliasedTupleSubsetResultTransformer {

    // IMPL NOTE : due to the delayed population of setters (setters cached
    //      for performance), we really cannot properly define equality for
    //      this transformer

    private static final long serialVersionUID = 3965277513953783745L;

    private final Class<?> resultClass;

    private boolean isInitialized;

    private String[] aliases;

    private String[] customFields;

    private Setter[] setters;

    public AliasToBeanResultTransformer(Class<?> resultClass,
                                        String[] customFields) {
        if (resultClass == null) {
            throw new IllegalArgumentException("resultClass cannot be null");
        }
        isInitialized = false;
        this.resultClass = resultClass;
        this.customFields = customFields;
    }

    @Override
    public boolean isTransformedValueATupleElement(String[] aliases,
                                                   int tupleLength) {
        return false;
    }

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        Object result;

        try {
            if (!isInitialized) {
                initialize(customFields);
            } else {
                check(customFields);
            }

            result = resultClass.newInstance();

            for (int i = 0; i < customFields.length; i++) {
                if (setters[i] != null) {
                    setters[i].set(result, tuple[i], null);
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new HibernateException("Could not instantiate resultclass: "
                    + resultClass.getName());
        }

        return result;
    }

    private void initialize(String[] aliases) {
        PropertyAccessStrategy accessStrategy =
                new PropertyAccessStrategyChainedImpl(
                        new PropertyAccessStrategyBasicImpl(),
                        new PropertyAccessStrategyFieldImpl()
                );
        this.aliases = new String[aliases.length];
        setters = new Setter[aliases.length];
        for (int i = 0; i < aliases.length; i++) {
            String alias = aliases[i];
            if (alias != null) {
                this.aliases[i] = alias;
                setters[i] = accessStrategy.buildPropertyAccess(resultClass, alias)
                        .getSetter();
            }
        }
        isInitialized = true;
    }

    private void check(String[] aliases) {
        if (!Arrays.equals(aliases, this.aliases)) {
            throw new IllegalStateException(
                    "aliases are different from what is cached; aliases="
                            + Arrays.asList(aliases) + " cached="
                            + Arrays.asList(this.aliases));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AliasToBeanResultTransformer that = (AliasToBeanResultTransformer) o;
        if (!resultClass.equals(that.resultClass)) {
            return false;
        }
        return Arrays.equals(aliases, that.aliases);
    }

    @Override
    public int hashCode() {
        int result = resultClass.hashCode();
        result = 31 * result + (aliases != null ? Arrays.hashCode(aliases) : 0);
        return result;
    }
}
