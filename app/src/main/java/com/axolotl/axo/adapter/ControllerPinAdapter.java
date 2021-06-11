package com.axolotl.axo.adapter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axolotl.axo.R;
import com.axolotl.axo.model.Pin;

import java.util.ArrayList;

public class ControllerPinAdapter extends RecyclerView.Adapter<ControllerPinAdapter.ViewHolder> {
    public Context context;
    public ArrayList<Pin> pins;
    public ArrayList<ImageView> imgButton;
    public ArrayList<TextView> pinNames;
    public ArrayList<TextView> pinStates;
    private PinInterface pinInterface;

    public ControllerPinAdapter(Context context, ArrayList<Pin> pins, PinInterface pinInterface) {
        this.context = context;
        this.pins = pins;
        this.pinInterface = pinInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.rv_controller_pin_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.pinName.setText(pins.get(position).name.replace("_", " "));
        holder.pinState.setText(pins.get(position).state.toUpperCase());
        holder.imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newState = "off";
                if (pins.get(position).state.equals("off")) {
                    newState = "on";
                }
                pinInterface.switchState(pins.get(position).id, newState);
            }
        });
        holder.pinName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinInterface.changePinName(pins.get(position).id);
            }
        });

        if (pins.get(position).state.equals("off"))
            holder.imgView.setImageResource(R.drawable.img_off);
        else
            holder.imgView.setImageResource(R.drawable.img_on);
    }

    @Override
    public int getItemCount() {
        return pins.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView;
        TextView pinName;
        TextView pinState;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgView = itemView.findViewById(R.id.rvControllerPinSwitchButton);
            pinName = itemView.findViewById(R.id.rvControllerPinNameTextView);
            pinState = itemView.findViewById(R.id.rvControllerPinStateTextView);
        }
    }

    public interface PinInterface {
        void switchState(String pinID, String newState);

        void changePinName(String pinID);
    }
}
