package core;

public class Rule {
    private String fullName;
    private String namespace;
    private String name;
    private Term pattern;
    private Term replacement;
    private int id;
    private int references;

    private static int nextId = 0;

    public Rule(Term pattern, Term replacement) {
        this(null, pattern, replacement);
    }

    public Rule(String fullName, Term pattern, Term replacement) {
        this.fullName = fullName;
        this.pattern = pattern;
        this.replacement = replacement;
        this.id = nextId++;
        this.references = 0;

        if (fullName != null && fullName.contains(".")) {
            int dotIndex = fullName.lastIndexOf(".");
            this.namespace = fullName.substring(0, dotIndex);
            this.name = fullName.substring(dotIndex + 1);
        } else {
            this.namespace = null;
            this.name = fullName;
        }
    }

    public String getFullName() {
        return fullName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public Term getPattern() {
        return pattern;
    }

    public Term getReplacement() {
        return replacement;
    }

    public int getId() {
        return id;
    }

    public int getReferences() {
        return references;
    }

    public void incrementReferences() {
        references++;
    }

    public boolean isAnonymous() {
        return fullName == null;
    }

    public boolean hasNamespace() {
        return namespace != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        if (fullName != null) {
            sb.append(fullName);
        }
        sb.append("> ");
        sb.append(pattern.toString());
        sb.append(" ");
        sb.append(replacement.toString());
        return sb.toString();
    }
}