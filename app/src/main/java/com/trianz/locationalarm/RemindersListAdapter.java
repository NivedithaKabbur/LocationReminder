package com.trianz.locationalarm;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.trianz.locationalarm.Utils.NamedGeofence;

import java.util.List;

public class RemindersListAdapter extends RecyclerView.Adapter<RemindersListAdapter.ViewHolder> {

  // region Properties

  private List<NamedGeofence> namedGeofences;

  private AllGeofencesAdapterListener listener;

  public void setListener(AllGeofencesAdapterListener listener) {
    this.listener = listener;
  }

  // endregion

  // Constructors

  public RemindersListAdapter(List<NamedGeofence> namedGeofences) {
    this.namedGeofences = namedGeofences;
  }

  // endregion

  // region Overrides

  @Override
  public RemindersListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.my_reminders_item_view, parent, false);
    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    final NamedGeofence geofence = namedGeofences.get(position);

    holder.place.setText(geofence.reminder_place);
    holder.message.setText(geofence.reminder_msg);
    holder.reminder_delete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setMessage(R.string.AreYouSure)
                .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                    if (listener != null) {
                      listener.onDeleteTapped(geofence);
                    }
                  }
                })
                .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                  }
                })
                .create()
                .show();

      }
    });

  }

  @Override
  public int getItemCount() {
    return namedGeofences.size();
  }

  // endregion

  // region Interfaces

  public interface AllGeofencesAdapterListener {
    void onDeleteTapped(NamedGeofence namedGeofence);
  }

  // endregion

  // region Inner classes

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView place;
    TextView message;
    ImageView reminder_delete;


    public ViewHolder(ViewGroup v) {
      super(v);

      place = (TextView) v.findViewById(R.id.reminder_place);
      message = (TextView) v.findViewById(R.id.reminder_message);
      reminder_delete = (ImageView) v.findViewById(R.id.reminder_delete);

    }
  }

}