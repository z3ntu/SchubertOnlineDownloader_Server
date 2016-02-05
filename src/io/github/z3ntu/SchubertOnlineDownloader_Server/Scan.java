package io.github.z3ntu.SchubertOnlineDownloader_Server;


public class Scan {
    private final String filename;
    private final String D_number;

    public Scan(String filename, String d_number) {
        this.filename = filename;
        this.D_number = d_number;
    }

    public String getFilename() {
        return this.filename;
    }

    public String getD_number() {
        return this.D_number;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        Scan scan = (Scan) o;

        return (this.filename.equals(scan.filename)) && (this.D_number.equals(scan.D_number));
    }


    public int hashCode() {
        int result = this.filename.hashCode();
        result = 31 * result + this.D_number.hashCode();
        return result;
    }
}
