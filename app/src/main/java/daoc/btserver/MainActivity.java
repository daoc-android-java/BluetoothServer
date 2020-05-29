package daoc.btserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.UUID;

import daoc.msg.SerialBmp;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/*
 * Atención: este código casi no hace control de errores
 * 
 */
public class MainActivity extends Activity {

	private static final UUID MY_UUID = UUID.fromString("00000000-1111-2222-3333-444444444444");
	private static final String NAME = "BtServer";
	
	private BluetoothAdapter btAdapter;
	private BluetoothServerSocket srvSocket;
	private BluetoothSocket socket;
	private ProcesaThread procesa;
	private ImageView iv;
	private Button bConectar;
	private Bitmap bmp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		iv = (ImageView) findViewById(R.id.iv1);
		bConectar = (Button) findViewById(R.id.bConectar);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if(btAdapter == null) {
			Toast.makeText(this, "ERROR: NO puede aceptar conexiones", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "Oprima el botón para aceptar conexión", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		desconectar(null);
	}
	
	public void conectar(View v) {
		try {
			srvSocket = btAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			socket = srvSocket.accept();
			srvSocket.close();
			srvSocket = null;
			procesa = new ProcesaThread();
			procesa.activa = true;
			procesa.start();
			bConectar.setEnabled(false);
			Toast.makeText(this, "Conectado al cliente", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "ERROR: NO puede aceptar conexiones", Toast.LENGTH_LONG).show();
		}
	}

	public void desconectar(View v) {
		try {
			bConectar.setEnabled(true);
			if(socket != null) {
				socket.close();
				socket = null;
			}
			if(procesa != null) {
				procesa.activa = false;
				procesa.interrupt();
				procesa = null;				
			}
			Toast.makeText(this, "Desconectado del cliente", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "ERROR: al desconectar", Toast.LENGTH_LONG).show();
		}
	}
	
	private class ProcesaThread extends Thread {
		public volatile boolean activa = true;
		
		public void run() {
			try {
				while(activa) {
					Log.d("PREVIEW", "Listo");
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream());	
					SerialBmp msg = (SerialBmp) in.readObject();
//					if(bmp != null) { 
//						bmp.recycle(); bmp = null;
//					}
					Log.d("PREVIEW", Integer.toString(msg.barr.length));
					bmp = BitmapFactory.decodeByteArray(msg.barr, 0, msg.barr.length);
					iv.post(new Runnable() {
					    public void run() {
					    	iv.setImageBitmap( bmp );
					    }
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
				MainActivity.this.runOnUiThread(new Runnable() {
				    public void run() {
				    	desconectar(null);
				    }
				});
			}
		}  		
		
	}
	
}

