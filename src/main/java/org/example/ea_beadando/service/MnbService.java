package org.example.ea_beadando.service;

import hu.mnb.soap.MNBArfolyamServiceSoap;
import hu.mnb.soap.MNBArfolyamServiceSoapImpl;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class MnbService {

    public record Rate(String date, double value) {
    }

    private MNBArfolyamServiceSoap port() {
        return new MNBArfolyamServiceSoapImpl().getCustomBindingMNBArfolyamServiceSoap();
    }

    public List<String> getCurrencies() throws Exception {
        NodeList nodes = parse(port().getCurrencies()).getElementsByTagName("Curr");
        List<String> result = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            result.add(nodes.item(i).getTextContent());
        }
        return result;
    }

    public List<Rate> getRates(String start, String end, String currency) throws Exception {
        String xml = port().getExchangeRates(start, end, currency);
        NodeList days = parse(xml).getElementsByTagName("Day");
        List<Rate> result = new ArrayList<>();
        for (int i = 0; i < days.getLength(); i++) {
            Element day = (Element) days.item(i);
            Element rate = (Element) day.getElementsByTagName("Rate").item(0);
            if (rate != null) {
                double value = Double.parseDouble(rate.getTextContent().replace(',', '.'));
                result.add(new Rate(day.getAttribute("date"), value));
            }
        }
        return result;
    }

    private Document parse(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
    }
}
