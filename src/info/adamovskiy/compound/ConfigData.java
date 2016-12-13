package info.adamovskiy.compound;

public class ConfigData {
    public final String name;
    public final String typeName;
    public final String modeOverride;

    public ConfigData(String name, String typeName, String modeOverride) {
        this.name = name;
        this.typeName = typeName;
        this.modeOverride = modeOverride;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigData that = (ConfigData) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (typeName != null ? !typeName.equals(that.typeName) : that.typeName != null) return false;
        return modeOverride != null ? modeOverride.equals(that.modeOverride) : that.modeOverride == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (typeName != null ? typeName.hashCode() : 0);
        result = 31 * result + (modeOverride != null ? modeOverride.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConfigData{" +
                "name='" + name + '\'' +
                ", typeName='" + typeName + '\'' +
                ", modeOverride='" + modeOverride + '\'' +
                '}';
    }
}
