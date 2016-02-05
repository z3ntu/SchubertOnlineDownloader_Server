package io.github.z3ntu.SchubertOnlineDownloader_Server;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {

    private static final String URL = "http://www.schubert-online.at/activpage/scans";
    private static final String MYSQL_HOST = "localhost";
    private static final String MYSQL_DB = "schubertonline";
    private static final String MYSQL_USER = "schubertonline";
    private static final String MYSQL_PASS = "45PbLZ9fEvn5ZQAH";
    private final HttpClient client = HttpClientBuilder.create().build();
    private final ArrayList<Scan> scans = new ArrayList<>();

    public static void main(String[] args) {
        Main main = new Main();
        main.loadScans();

        main.sendToDatabase();
    }

    private void sendToDatabase() {
        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + MYSQL_HOST + ":3306/" + MYSQL_DB, MYSQL_USER, MYSQL_PASS);
            statement = connection.createStatement();
            StringBuilder insert = new StringBuilder("INSERT INTO scans (d_nr, filename) VALUES ");
            for (Scan scan : this.scans) {
                insert.append("('").append(scan.getD_number()).append("', '").append(scan.getFilename()).append("'),");
            }
            insert.setLength(insert.length() - 1);
            statement.execute("TRUNCATE scans");
            int ret = statement.executeUpdate(insert.toString());
            System.out.println("\nDatasets changed: " + ret);
            System.out.println("Sent to database!");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.out.println("Error while sending to database!");
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadScans() {
        try {
            HttpGet httpGet = new HttpGet(URL);
            HttpResponse response = this.client.execute(httpGet);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            Document doc = Jsoup.parse(result.toString());
            Elements elements = doc.select("body > table > tbody > tr");

            Pattern d_NR = Pattern.compile("_D[\\d]*[\\w]?_");
            Pattern f_NR = Pattern.compile(",F.%[\\d]*_");
            Pattern MH_ = Pattern.compile("MH_[\\d]*_");
            Pattern SBB_ = Pattern.compile("SBB_[\\d]*_");
            Pattern MH_2 = Pattern.compile("MH_[\\d]*\\(2\\)_");

            int index = 0;

            for (Element tr : elements) {
                if ((index >= 3) && (index < elements.size() - 1)) {
                    Element td = tr.child(1);
                    Element a = td.child(0);
                    String link = a.attr("href");


                    Matcher matcher_d_nr = d_NR.matcher(link);
                    if (matcher_d_nr.find()) {
                        String d_nr = matcher_d_nr.group().replaceAll("_", "");
                        if (!d_nr.equals("D")) {
                            Scan scan = new Scan(link, d_nr);
                            if (!this.scans.contains(scan)) {
                                this.scans.add(scan);
                            }
                        }
                    } else {
                        Matcher matcher_f_nr = f_NR.matcher(link);
                        if (matcher_f_nr.find()) {
                            String f_nr = matcher_f_nr.group().replaceAll("[,%_]", "");
                            Scan scan = new Scan(link, f_nr);
                            if (!this.scans.contains(scan)) {
                                this.scans.add(scan);
                            }
                        } else {
                            Matcher matcher_mh_ = MH_.matcher(link);
                            if (matcher_mh_.find()) {
                                String mh_nr = matcher_mh_.group();
                                Scan scan = new Scan(link, mh_nr);
                                if (!this.scans.contains(scan)) {
                                    this.scans.add(scan);
                                }
                            } else {
                                Matcher matcher_sbb_ = SBB_.matcher(link);
                                if (matcher_sbb_.find()) {
                                    String sbb_nr = matcher_sbb_.group();
                                    Scan scan = new Scan(link, sbb_nr);
                                    if (!this.scans.contains(scan)) {
                                        this.scans.add(scan);
                                    }
                                } else {
                                    Matcher matcher_mh_2 = MH_2.matcher(link);
                                    if (matcher_mh_2.find()) {
                                        String mh_2_nr = matcher_mh_2.group();
                                        Scan scan = new Scan(link, mh_2_nr);
                                        if (!this.scans.contains(scan)) {
                                            this.scans.add(scan);
                                        }
                                    } else {
                                        System.out.println("Skip: " + link);
                                    }
                                }
                            }
                        }
                    }
                }
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printAllScans() {
        System.out.println("Size: " + this.scans.size());
        for (Scan scan : this.scans) {
            System.out.println(scan.getD_number() + " - " + scan.getFilename());
        }
    }
}

