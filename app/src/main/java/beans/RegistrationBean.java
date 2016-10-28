package beans;

/**
 * Created by abhi on 6/30/2016.
 */
public class RegistrationBean {

    private int id;
    private String rfid;
    private String vehicleNumber;
    private String employeeName;
    private String employeeNumber;

    public RegistrationBean() {
    }

    public RegistrationBean(String rfid, String vehicleNumber, String employeeName, String employeeNumber) {
        this.rfid = rfid;
        this.vehicleNumber = vehicleNumber;
        this.employeeName = employeeName;
        this.employeeNumber = employeeNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }
}
