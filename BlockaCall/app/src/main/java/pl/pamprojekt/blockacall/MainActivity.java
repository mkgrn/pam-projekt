package pl.pamprojekt.blockacall;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnClickListener, OnItemLongClickListener {

    // Declaration all on screen components of the Main screen
    private Button add_blacklist_btn;
    public ListView listview;

    // Object of BlacklistDAO to query to database
    private BlacklistDAO blackListDao;

    // It holds the list of Blacklist objects fetched from Database
    public static List<Blacklist> blockList;


    // This holds the value of the row number, which user has selected for further action
    private int selectedRecordPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialization of the button of the Main screen
        add_blacklist_btn = (Button) findViewById(R.id.add_blacklist_btn);

        // Attachment of onClickListner for it
        add_blacklist_btn.setOnClickListener(this);

        // Initialization of the listview of the Main screen to display black listed phone numbers
        listview = (ListView) findViewById(R.id.listview);

        // Set the header of the ListView
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.list_item, listview, false);
        listview.addHeaderView(rowView);

        // Attach OnItemLongClickListener to track user action and perform accordingly
        listview.setOnItemLongClickListener(this);
    }

    private void populateNoRecordMsg()
    {
        // If, no record found in the database, appropriate message needs to be displayed.
        if(blockList.size() == 0)
        {
            final TextView tv = new TextView(this);
            tv.setPadding(5, 5, 5, 5);
            tv.setTextSize(15);
            tv.setText("No Record Found !!");
            listview.addFooterView(tv);
        }
    }

    @Override
    public void onClick(View v) {
        // Render AddToBlocklistActivity screen once click on "Add" Button
        if (v == add_blacklist_btn) {
            startActivity(new Intent(this, AddToBlocklistActivity.class));
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // If the pressed row is not a header, update selectedRecordPosition and
        // show dialog for further selection
        if (position > 0) {
            selectedRecordPosition = position - 1;
            showDialog();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initialize the DAO object
        blackListDao = new BlacklistDAO(this);

        // Fetch the list of Black listed numbers from Database using DAO object
        blockList = blackListDao.getAllBlacklist();

        // Remove the footer view
        if(listview.getChildCount() > 1)
            listview.removeFooterView(listview.getChildAt(listview.getChildCount() - 1));

        //Now, link the  CustomArrayAdapter with the ListView
        listview.setAdapter(new CustomArrayAdapter(this, R.layout.list_item, blockList));

        // If, no record found in the database, appropriate message needs to be displayed.
        populateNoRecordMsg();
    }

    private void showDialog()
    {
        // Before deletion of the long pressed record, need to confirm with the user. So, build the AlartBox first
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // Set the appropriate message into it.
        alertDialogBuilder.setMessage("Are you Really want to delete the selected record ?");

        // Add a positive button and it's action. In our case action would be deletion of the data
        alertDialogBuilder.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        try {

                            blackListDao.delete(blockList.get(selectedRecordPosition));

                            // Removing the same from the List to remove from display as well
                            blockList.remove(selectedRecordPosition);
                            listview.invalidateViews();

                            // Reset the value of selectedRecordPosition
                            selectedRecordPosition = -1;
                            populateNoRecordMsg();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        // Add a negative button and it's action. In our case, just hide the dialog box
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        // Now, create the Dialog and show it.
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public class CustomArrayAdapter extends ArrayAdapter<String> {

        private LayoutInflater inflater;

        // This would hold the database objects i.e. Blacklist
        private List<Blacklist> records;

        @SuppressWarnings("unchecked")
        public CustomArrayAdapter(Context context, int resource, @SuppressWarnings("rawtypes") List objects) {
            super(context, resource, objects);

            this.records = objects;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            //Reuse the view to make the scroll effect smooth
            if(convertView == null)
                convertView = inflater.inflate(R.layout.list_item, parent, false);

            // Fetch phone number from the database object
            final Blacklist phoneNumber =  records.get(position);

            // Set to screen component to display results
            ((TextView)convertView.findViewById(R.id.serial_tv)).setText("" + (position +1));
            ((TextView)convertView.findViewById(R.id.phone_number_tv)).setText(phoneNumber.phoneNumber);
            return convertView;
        }

    }
}