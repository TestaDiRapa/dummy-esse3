package payloads;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EventPayload {

    @XmlElement public String description;
    @XmlElement public String type;
    @XmlElement public String date;
    @XmlElement public String professor;
    @XmlElement public String password;
}
