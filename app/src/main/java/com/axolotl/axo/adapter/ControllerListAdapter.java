package com.axolotl.axo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.axolotl.axo.R;
import com.axolotl.axo.model.Controller;

import java.util.ArrayList;

import com.axolotl.axo.utility.*;

public class ControllerListAdapter extends RecyclerView.Adapter<ControllerListAdapter.ViewHolder> {
    public Context context;
    public ArrayList<Controller> controllers;
    public ArrayList<LinearLayout> layouts = new ArrayList<>();
    public int selectedIndex = -1;
    private SelectController selectController;

    public ControllerListAdapter(Context context, ArrayList<Controller> controllers, SelectController selectController) {
        this.context = context;
        this.controllers = controllers;
        this.selectController = selectController;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.rv_controller_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(controllers.get(position).name.replace("_", " "));
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedIndex = position;
                selectController.onSelect(controllers.get(selectedIndex));
            }
        });
        if (selectedIndex == position) {
            holder.layout.setBackgroundResource(R.drawable.bg_controller_list_item_selected);
            holder.constraintLayout.requestFocus();
        } else {
//            holder.layout.setBackgroundResource(R.color.black_obsidian);
            holder.layout.setBackgroundResource(R.drawable.bg_controller_list_item);
        }
    }

    @Override
    public int getItemCount() {
        return controllers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        LinearLayout layout;
        ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.rvControllerListTextView);
            layout = itemView.findViewById(R.id.rvControllerListLayout);
            constraintLayout = itemView.findViewById(R.id.rvControllerListConstraintLayout);
        }
    }

    public void setActiveController(String controllerID) {
        ArrayList<String> controllerIDs = new ArrayList<String>();
        for (Controller controller : controllers) {
            controllerIDs.add(controller.id);
        }
        this.selectedIndex = controllerIDs.indexOf(controllerID);
    }

    public interface SelectController {
        void onSelect(Controller controller);
    }
}
