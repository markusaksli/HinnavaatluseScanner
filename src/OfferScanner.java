import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class OfferScanner {
    private String pageUrl;
    private String name;
    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner(System.in);
        System.out.println("Sisesta toote URL:");
        OfferScanner r = new OfferScanner();
        r.pageUrl = s.nextLine();
        System.out.println("Anna teavituseks tootele nimi:");
        r.name = s.nextLine();
        System.out.println("Sisesta uuendamise intervall minutites (täisarv):");
        int rTime = s.nextInt();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        List<Offer> oldPakkumised = r.refresh();
        System.out.println("Pakkumised:\r\n");
        r.printIt(oldPakkumised,dtf.format(now));
        System.out.println("Uuendan iga " + rTime+" minuti tagant...\r\n");
        boolean changed = false;
        while (true) {
            try {
                TimeUnit.MINUTES.sleep(rTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<Offer> pakkumised = r.refresh();
            if (pakkumised.size() != oldPakkumised.size()) {
                System.out.println("Pakkumiste arv on muutunud: " + (pakkumised.size() - oldPakkumised.size()) + "\r\n");
                changed = true;
            } else {
                for (int i = 0; i < pakkumised.size(); i++) {
                    if (!pakkumised.get(i).equals(oldPakkumised.get(i))) {
                        System.out.println(oldPakkumised.get(i).name + " pakkumine on muutunud");
                        changed = true;
                    }
                }
            }
            if (changed) {
                System.out.println("Uued pakkumised:\r\n");
                r.notificate();
                now = LocalDateTime.now();
                r.printIt(pakkumised, dtf.format(now));
                oldPakkumised = pakkumised;
                changed = false;
            }
        }
    }

    private void printIt(List<Offer> pakkumised, String time){
        StringBuilder info = new StringBuilder();
        info.append(time).append("\r\n");
        for(Offer s: pakkumised){
            info.append(s.name).append("   ").append(s.price).append("   ").append(s.aval).append("\r\n");
        }
        System.out.println(info);
    }

    private List<Offer> refresh() throws IOException {
        List<Element> pakkumised =
                Jsoup.connect(pageUrl)
                        .get().getElementById("product-offers-items")
                        .getElementsByClass("extra-offers-wrap");
        List<Offer> infos = new ArrayList<>();
        for (Element tempElement : pakkumised) {
            try {
                Offer infobits = new Offer(
                        tempElement.getElementsByClass("extra-offers-company-logo").get(0).getElementsByTag("a").attr("title"),
                        tempElement.getElementsByClass("bold roboto red text-24 extra-offers-price nomargin curr_change").eachText().get(0),
                        tempElement.getElementsByClass("extra-offers-availability").eachText().get(0)
                );
                infos.add(infobits);
            } catch (Exception ex) {
                Offer infobits = new Offer(
                        tempElement.getElementsByClass("extra-offers-company-logo").get(0).getElementsByTag("a").attr("title"),
                        tempElement.getElementsByClass("bold roboto red text-24 extra-offers-price nomargin curr_change").eachText().get(0),
                        ""
                );
                infos.add(infobits);
            }
        }
        return infos;
    }

    private void notificate() {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("some-icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "Scanner pakkumised");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Scanner teavitus");
            tray.add(trayIcon);
            trayIcon.displayMessage(name + " Scanner", "Pakkumised on muutunud!", MessageType.WARNING);
        } catch (Exception ex) {
            System.err.print(ex);
        }
    }
}