package org.jenkinsci.plugins.lucene.search;

import hudson.model.Run;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Field {

    PROJECT_NAME("projectname", Persist.TRUE) {
        public String getValue(final Run<?, ?> build) {
            StringBuilder builder = new StringBuilder();
            if (!build.getParent().getParent().getDisplayName().equalsIgnoreCase("jenkins")) {
                builder.append(build.getParent().getParent().getFullName()).append("/");
            }
            builder.append(build.getParent().getName());
            return builder.toString();
        }
    },

    BUILD_NUMBER("buildnumber", DefaultSearchable.FALSE, Numeric.TRUE, Persist.TRUE) {
        @Override
        public String getValue(Run<?, ?> build) {
            return String.valueOf(build.getNumber());
        }
    },

    START_TIME("starttime", DefaultSearchable.FALSE, Numeric.TRUE, Persist.TRUE) {
        @Override
        public Long getValue(Run<?, ?> build) {
            return build.getStartTimeInMillis();
        }
    },

    CONSOLE("console", Persist.TRUE) {
        @Override
        public String getValue(Run<?, ?> build) {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                build.getLogText().writeLogTo(0, byteArrayOutputStream);
                return byteArrayOutputStream.toString();
            } catch (IOException e) {
                return null;
            }
        }
    };

    private static Map<String, Field> index;
    public final String fieldName;
    public final boolean defaultSearchable;
    public final boolean numeric;
    public final boolean persist;

    @SuppressWarnings("rawtypes")
    Field(String fieldName, Enum... e) {
        List<Enum> es = Arrays.asList(e);
        defaultSearchable = !es.contains(DefaultSearchable.FALSE);
        numeric = es.contains(Numeric.TRUE);
        persist = es.contains(Persist.TRUE);
        this.fieldName = fieldName;
    }

    public static Field getIndex(String fieldName) {
        if (index == null) {
            Map<String, Field> indexReverseLookup = new HashMap<String, Field>();
            for (Field idx : Field.values()) {
                indexReverseLookup.put(idx.fieldName, idx);
            }
            index = indexReverseLookup;
        }
        return index.get(fieldName);
    }

    public abstract Object getValue(final Run<?, ?> build);

    private enum Persist {
        TRUE;
    }

    private enum DefaultSearchable {
        FALSE;
    }

    private enum Numeric {
        TRUE;
    }
}
