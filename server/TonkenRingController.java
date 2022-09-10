public class TonkenRingController {
    
public enum ErrorControlType {
    ACK("ACK"), NAK("NAK")

    public String value; 

    public ErrorControlType(String value) {
        this.value = value
    }

} 

    // "{DataPacket.CODE};{errorControlType.value}:{originAddr}:{resourceName}:{(ip;hash),(ip;hash)}"
}
