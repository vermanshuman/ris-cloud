package it.nexera.ris.persistence.types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateType implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[]{
                Types.TIMESTAMP
        };
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class returnedClass() {
        return Date.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == null && y == null) {
            return true;
        } else if (x == null && y != null) {
            return false;
        } else if (y != null) {
            return x.equals(y);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode(Object value) throws HibernateException {
        return value.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names,
                              SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
        Timestamp value = rs.getTimestamp(names[0]);

        if (value == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(value.getTime());
        TimeZone fromTimeZone = TimeZone.getTimeZone("UTC");
        TimeZone toTimeZone = calendar.getTimeZone();

        calendar.setTimeZone(fromTimeZone);
        calendar.add(Calendar.MILLISECOND, fromTimeZone.getRawOffset() * -1);
        if (fromTimeZone.inDaylightTime(calendar.getTime())) {
            calendar.add(Calendar.MILLISECOND, calendar.getTimeZone()
                    .getDSTSavings() * -1);
        }

        calendar.add(Calendar.MILLISECOND, toTimeZone.getRawOffset());
        if (toTimeZone.inDaylightTime(calendar.getTime())) {
            calendar.add(Calendar.MILLISECOND, toTimeZone.getDSTSavings());
        }

        calendar.setTimeZone(toTimeZone);

        return calendar.getTime();
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index,
                            SessionImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.DATE);

            return;
        }

        if (!(value instanceof java.util.Date)) {
            throw new UnsupportedOperationException("can't convert "
                    + value.getClass());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(((java.util.Date) value).getTime());
        TimeZone fromTimeZone = calendar.getTimeZone();
        TimeZone toTimeZone = TimeZone.getTimeZone("UTC");

        calendar.setTimeZone(fromTimeZone);
        calendar.add(Calendar.MILLISECOND, fromTimeZone.getRawOffset() * -1);
        if (fromTimeZone.inDaylightTime(calendar.getTime())) {
            calendar.add(Calendar.MILLISECOND, calendar.getTimeZone()
                    .getDSTSavings() * -1);
        }

        calendar.add(Calendar.MILLISECOND, toTimeZone.getRawOffset());
        if (toTimeZone.inDaylightTime(calendar.getTime())) {
            calendar.add(Calendar.MILLISECOND, toTimeZone.getDSTSavings());
        }

        calendar.setTimeZone(toTimeZone);

        value = calendar.getTime();

        st.setTimestamp(index,
                new java.sql.Timestamp(((java.util.Date) value).getTime()));
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        }
        if (!(value instanceof java.util.Date)) {
            throw new UnsupportedOperationException("can't convert "
                    + value.getClass());
        }
        return new Date(((java.util.Date) value).getTime());
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        if (!(value instanceof java.util.Date)) {
            throw new UnsupportedOperationException("can't convert "
                    + value.getClass());
        }
        return (java.util.Date) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner)
            throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner)
            throws HibernateException {
        return original;
    }

}
