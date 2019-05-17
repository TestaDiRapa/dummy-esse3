package professors.payloads;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ProfessorRegistrationPayload {

    @XmlElement public String name;
    @XmlElement public String surname;
    @XmlElement public String username;
    @XmlElement public String pwd;

}
