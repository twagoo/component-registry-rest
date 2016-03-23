package clarin.cmdi.componentregistry;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public enum CmdVersion {
    CMD_1_1("CMDI 1.1"),
    CMD_1_2("CMDI 1.2");

    public final static CmdVersion CANONICAL_CMD_VERSION = CmdVersion.CMD_1_2;

    private final String versionName;

    private CmdVersion(String versionName) {
        this.versionName = versionName;
    }

    @Override
    public String toString() {
        return versionName;
    }

}
