package payloads;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IdentificationPayload {

    @XmlElement public String username;
    @XmlElement public String pwd;

}
