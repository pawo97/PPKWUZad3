package com.example.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;
import net.fortuna.ical4j.validate.ValidationException;

@RestController
public class WebController {

    @RequestMapping("calendar")
    public ResponseEntity<String> response(@RequestParam(value = "year") String Yyear, @RequestParam(value = "month") String Ymonth)
            throws IOException, ValidationException {

        Calendar c = new Calendar();
        c = getCalendarWeeia(Yyear, Ymonth);
        c.getProperties()
         .add(new ProdId("-//Bean Fortune //iCal4j 2.0//EN"));
        c.getProperties()
         .add(Version.VERSION_2_0);
        c.getProperties()
         .add(CalScale.GREGORIAN);
        c.validate();
        return new ResponseEntity<>(c.toString(), HttpStatus.OK);
    }

    private Calendar getCalendarWeeia(String yyear, String ymonth) throws IOException {

        String content = "";

        String urlS = "http://www.weeia.p.lodz.pl/pliki_strony_kontroler/kalendarz.php?";
        urlS += "rok=" + yyear + "&miesiac=" + ymonth + "&lang=1";

        URL website = new URL(urlS);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        content = response.toString();

        Document doc = Jsoup.parse(content, "UTF-8");
        Elements active = doc.select("td.active");

        Elements e = active.select("div.InnerBox");
        Elements f = active.select("a.active");

        List<Integer> days = new ArrayList();
        List<String> events = new ArrayList();
        for (int i = 0; i < e.size(); i++) {
            String pom = e.get(i)
                          .toString()
                          .substring(27, e.get(i)
                                          .toString()
                                          .length()
                                         - 11);
            events.add(pom);
            int d = Integer.valueOf(f.get(i)
                                     .toString()
                                     .substring(44, f.get(i)
                                                     .toString()
                                                     .length()
                                                    - 4));
            days.add(d);
        }
        java.util.Calendar cal = java.util.Calendar.getInstance();
        Calendar calendar = new Calendar();

        for (int i = 0; i < days.size(); i++) {
            cal.set(java.util.Calendar.YEAR, Integer.valueOf(yyear));
            cal.set(java.util.Calendar.MONTH, Integer.valueOf(ymonth) - 1);
            cal.set(java.util.Calendar.DAY_OF_MONTH, days.get(i));
            UidGenerator ug = null;
            ug = new UidGenerator("1");
            VEvent event = new VEvent(new Date(cal.getTime()), events.get(i));
            event.getProperties()
                 .add(ug.generateUid());
            calendar.getComponents()
                    .add(event);
        }
        return calendar;
    }
}
