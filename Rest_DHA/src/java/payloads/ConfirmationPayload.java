package payloads;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ConfirmationPayload {

    @XmlElement public String username;
    @XmlElement public String pwd;
    @XmlElement public String student;

}
