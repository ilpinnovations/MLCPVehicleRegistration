package beans;

import java.util.ArrayList;

/**
 * Created by abhi on 6/30/2016.
 */
public class JsonDataBean {
    private ArrayList<RegistrationBean> data;

    public JsonDataBean(ArrayList<RegistrationBean> data) {
        this.data = data;
    }

    public ArrayList<RegistrationBean> getData() {
        return data;
    }

    public void setData(ArrayList<RegistrationBean> data) {
        this.data = data;
    }
}
