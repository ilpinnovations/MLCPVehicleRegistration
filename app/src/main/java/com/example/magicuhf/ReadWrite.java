package com.example.magicuhf;

import android.app.Activity;
import android.hardware.uhf.magic.reader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ReadWrite extends Activity {
	Button m_btnDubiaoqian,m_btnXiebiaoqian;
	EditText m_editAddress,m_editLength,m_editInput,m_editmima;
	TextView m_result;
	String m_strresult;
	ArrayAdapter<String> m_adapter;
	Spinner m_spinner;
	private Handler mHandler = new MainHandler();  
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.readwrite);
		m_btnDubiaoqian=(Button)findViewById(R.id.dubiaoqian);
		m_btnXiebiaoqian=(Button)findViewById(R.id.xiebiaoqian);
		m_editAddress=(EditText)findViewById(R.id.address);
		m_editLength=(EditText)findViewById(R.id.datalength);
		m_editInput=(EditText)findViewById(R.id.inputdata);
		m_editmima=(EditText)findViewById(R.id.password);
		m_result=(TextView)findViewById(R.id.resultView);
		m_spinner=(Spinner)findViewById(R.id.spinner1);
		String []str={"USER","EPC","TID","RFU"};
		m_adapter=new ArrayAdapter<String>(ReadWrite.this,android.R.layout.simple_spinner_item,str);
		m_adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		m_spinner.setAdapter(m_adapter);
		reader.m_handler=mHandler;
		m_btnDubiaoqian.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				m_strresult=""; 
				m_result.setText(m_strresult);
				byte btMemBank=(byte)m_spinner.getSelectedItemPosition();

			
				String stradd = m_editAddress.getText().toString().trim();
				int nadd=Integer.valueOf(stradd);
				String strdatalength = m_editLength.getText().toString().trim();
				int ndatalen=Integer.valueOf(strdatalength);
				String mimaStr = m_editmima.getText().toString().trim();
				if (mimaStr == null || mimaStr.equals("")) {
					m_strresult += "Please enter your 8 - digit password!!\n";
					m_result.setText(m_strresult);
					return;
				}
				byte[] passw = reader.stringToBytes(mimaStr);	
				byte[]epc=reader.stringToBytes(reader.m_strPCEPC);
				if(btMemBank==1)
					reader.ReadLables(passw, epc.length, epc, (byte)btMemBank, 1, ndatalen);
				else
				reader.ReadLables(passw, epc.length, epc, (byte)btMemBank, nadd, ndatalen);

			}
		});
		m_btnXiebiaoqian.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				m_strresult=""; 
				m_result.setText(m_strresult);
				byte btMemBank=(byte)m_spinner.getSelectedItemPosition();
				String stradd = m_editAddress.getText().toString().trim();
				int nadd=Integer.valueOf(stradd);
				String strdatalength = m_editLength.getText().toString().trim();
				int ndatalen=Integer.valueOf(strdatalength);
				String mimaStr = m_editmima.getText().toString().trim();
				if (mimaStr == null || mimaStr.equals("")) {
					m_strresult += "Please enter your 8 - digit password!!\n";
					m_result.setText(m_strresult);
					return;
				} 
				byte[] passw = reader.stringToBytes(mimaStr);
				byte[]pwrite=new byte[ndatalen];  
				String dataE = m_editInput.getText().toString().trim();
				byte[] myByte = reader.stringToBytes(dataE);
				System.arraycopy(myByte, 0, pwrite, 0, myByte.length > ndatalen ? ndatalen
						: myByte.length);   
				byte[]epc=reader.stringToBytes(reader.m_strPCEPC);
				reader.Writelables(passw,epc.length,epc, btMemBank, (byte)nadd, (byte)ndatalen, pwrite);
				
			}
		});
		
	}
	   private class MainHandler extends Handler {  
	    	@Override           
	    	public void handleMessage(Message msg) {
	    		if(msg.what!=0)         
	    		{  
	    			if(m_strresult.indexOf((String)msg.obj)<0)
	    			{  
	    				//Log.e("8888888888",(String)msg.obj+"\r\n");
						m_strresult +=(String)msg.obj;
		    			m_strresult+="\r\n";
		    			m_result.setText(m_strresult);

	    			}  

	    		}
	  
	    	}
	    }; 
}
