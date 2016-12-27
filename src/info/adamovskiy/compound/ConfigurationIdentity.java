package info.adamovskiy.compound;

import org.eclipse.jdt.annotation.NonNull;

import java.util.Objects;

public class ConfigurationIdentity {
    public final String name;
    public final String typeName;

    public ConfigurationIdentity(@NonNull String name, @NonNull String typeName) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(typeName);
        this.name = name;
        this.typeName = typeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConfigurationIdentity that = (ConfigurationIdentity) o;

        return name.equals(that.name) && typeName.equals(that.typeName);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + typeName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return typeName + "#" + name;
    }
}
