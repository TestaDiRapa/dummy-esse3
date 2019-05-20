package payloads;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeletionPayload {

    @XmlElement public String username;
    @XmlElement public String pwd;

}
