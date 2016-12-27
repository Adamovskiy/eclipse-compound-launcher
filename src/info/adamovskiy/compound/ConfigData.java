package info.adamovskiy.compound;

public class ConfigData {
    public final ConfigurationIdentity identity;
    public final String modeOverride;

    public ConfigData(ConfigurationIdentity identity, String modeOverride) {
        this.identity = identity;
        this.modeOverride = modeOverride;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConfigData that = (ConfigData) o;

        if (identity != null ? !identity.equals(that.identity) :
                that.identity != null) {
            return false;
        }
        return modeOverride != null ? modeOverride.equals(that.modeOverride) : that.modeOverride == null;
    }

    @Override
    public int hashCode() {
        int result = identity != null ? identity.hashCode() : 0;
        result = 31 * result + (modeOverride != null ? modeOverride.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConfigData{" + "identity=" + identity + ", modeOverride='" + modeOverride + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                '\'' + '}';
    }
}
