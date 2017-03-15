package com.trianz.locationalarm.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLngBounds;
import com.trianz.locationalarm.R;

/**
 * Created by Niveditha.Kabbur on 3/14/2017.
 */

public class CustomPlaceAutoCompleteFragment extends PlaceAutocompleteFragment {

    private View zzbmN;
    private View zzbmO;
    private EditText zzbmP;
    private boolean zzbmQ;
    @Nullable
    private LatLngBounds zzbmR;
    @Nullable
    private AutocompleteFilter zzbmS;
    @Nullable
    private PlaceSelectionListener zzbmT;

    public CustomPlaceAutoCompleteFragment() {
    }

    public View onCreateView(LayoutInflater var1, ViewGroup var2, Bundle var3) {
        View var4 = var1.inflate(R.layout.custom_place_auto_complete_layout, var2, false);
        this.zzbmN = var4.findViewById(R.id.search_button);
        this.zzbmO = var4.findViewById(R.id.search_clear_button);
        this.zzbmP = (EditText)var4.findViewById(R.id.search_place);
        View.OnClickListener var5 = new View.OnClickListener() {
            public void onClick(View var1) {
                if(!CustomPlaceAutoCompleteFragment.this.zzbmQ) {
                    CustomPlaceAutoCompleteFragment.this.zzIw();
                }

            }
        };
        this.zzbmN.setOnClickListener(var5);
        this.zzbmP.setOnClickListener(var5);
        this.zzbmO.setOnClickListener(new View.OnClickListener() {
            public void onClick(View var1) {
                CustomPlaceAutoCompleteFragment.this.setText("");
            }
        });
        this.zzIv();
        return var4;
    }

    public void onDestroyView() {
        this.zzbmN = null;
        this.zzbmO = null;
        this.zzbmP = null;
        super.onDestroyView();
    }

    public void setBoundsBias(@Nullable LatLngBounds var1) {
        this.zzbmR = var1;
    }

    public void setFilter(@Nullable AutocompleteFilter var1) {
        this.zzbmS = var1;
    }

    public void setText(CharSequence var1) {
        this.zzbmP.setText(var1);
        this.zzIv();
    }

    public void setHint(CharSequence var1) {
        this.zzbmP.setHint(var1);
        this.zzbmN.setContentDescription(var1);
    }

    public void setOnPlaceSelectedListener(PlaceSelectionListener var1) {
        this.zzbmT = var1;
    }

    private void zzIv() {
        boolean var1 = !this.zzbmP.getText().toString().isEmpty();
        if(var1)
        {
            this.zzbmO.setVisibility(View.VISIBLE);
        }
        else
        {
            this.zzbmO.setVisibility(View.INVISIBLE);
        }
    }

    private void zzIw() {
        int var1 = -1;

        try {
            Intent var2 = (new PlaceAutocomplete.IntentBuilder(2)).setBoundsBias(this.zzbmR).setFilter(this.zzbmS).zzfd(this.zzbmP.getText().toString()).zzkU(1).build(this.getActivity());
            this.zzbmQ = true;
            this.startActivityForResult(var2, 30421);
        } catch (GooglePlayServicesRepairableException var3) {
            var1 = var3.getConnectionStatusCode();
            Log.e("Places", "Could not open autocomplete activity", var3);
        } catch (GooglePlayServicesNotAvailableException var4) {
            var1 = var4.errorCode;
            Log.e("Places", "Could not open autocomplete activity", var4);
        }

        if(var1 != -1) {
            GoogleApiAvailability var5 = GoogleApiAvailability.getInstance();
            var5.showErrorDialogFragment(this.getActivity(), var1, 30422);
        }

    }

    public void onActivityResult(int var1, int var2, Intent var3) {
        this.zzbmQ = false;
        if(var1 == 30421) {
            if(var2 == -1) {
                Place var4 = PlaceAutocomplete.getPlace(this.getActivity(), var3);
                if(this.zzbmT != null) {
                    this.zzbmT.onPlaceSelected(var4);
                }

                this.setText(var4.getName().toString());
            } else if(var2 == 2) {
                Status var5 = PlaceAutocomplete.getStatus(this.getActivity(), var3);
                if(this.zzbmT != null) {
                    this.zzbmT.onError(var5);
                }
            }
        }

        super.onActivityResult(var1, var2, var3);
    }
}
