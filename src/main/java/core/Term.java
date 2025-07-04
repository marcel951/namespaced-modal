package core;

import java.util.ArrayList;
import java.util.List;

public class Term {
    private boolean isAtom;
    private String value;
    private List<Term> children;

    public Term(String value) {
        this.isAtom = true;
        this.value = value;
        this.children = null;
    }

    public Term(List<Term> children) {
        this.isAtom = false;
        this.value = null;
        this.children = new ArrayList<>(children);
    }

    public boolean isAtom() {
        return isAtom;
    }

    public boolean isList() {
        return !isAtom;
    }

    public String getValue() {
        return value;
    }

    public List<Term> getChildren() {
        return children;
    }

    public int size() {
        return isAtom ? 0 : children.size();
    }

    public boolean isVariable() {
        return isAtom && value.startsWith("?");
    }

    public String getVariableName() {
        return value.substring(1);
    }

    @Override
    public String toString() {
        if (isAtom) {
            return value;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < children.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(children.get(i).toString());
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Term other = (Term) obj;
        if (isAtom != other.isAtom) return false;

        if (isAtom) {
            return value.equals(other.value);
        }
        return children.equals(other.children);
    }
}