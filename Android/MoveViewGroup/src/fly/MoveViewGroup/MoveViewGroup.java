package fly.MoveViewGroup;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MoveViewGroup extends Activity implements View.OnClickListener {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
	 
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		TextView tv=(TextView)v;
		Toast.makeText(this, tv.getText(), Toast.LENGTH_SHORT).show(); 
		
	}
}