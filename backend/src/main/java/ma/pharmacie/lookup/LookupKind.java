package ma.pharmacie.lookup;

import ma.pharmacie.common.exception.NotFoundException;

/**
 * The four lookup dimensions exposed by the API. The {@link #path} value is the
 * URL segment used in {@code /api/v1/lookups/{path}} and matches the existing
 * read endpoints, so the frontend has a single base URL per kind.
 */
public enum LookupKind {

    FORMS              ("forms",                "pharmaceutical_form",       "forme galénique"),
    AGE_GROUPS         ("age-groups",           "age_group",                 "tranche d'âge"),
    THERAPEUTIC_CLASSES("therapeutic-classes",  "therapeutic_class",         "classe thérapeutique"),
    INDICATIONS        ("indications",          "indication",                "indication");

    private final String path;
    private final String tableName;
    private final String labelFr;

    LookupKind(String path, String tableName, String labelFr) {
        this.path = path;
        this.tableName = tableName;
        this.labelFr = labelFr;
    }

    public String path()      { return path; }
    public String tableName() { return tableName; }
    public String labelFr()   { return labelFr; }

    /** Parse the path segment, throwing 404 if unknown. */
    public static LookupKind fromPath(String s) {
        for (LookupKind k : values()) {
            if (k.path.equalsIgnoreCase(s)) return k;
        }
        throw new NotFoundException("Unknown lookup kind: " + s);
    }
}

