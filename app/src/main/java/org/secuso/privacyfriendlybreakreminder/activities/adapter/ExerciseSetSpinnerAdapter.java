package org.secuso.privacyfriendlybreakreminder.activities.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.database.data.ExerciseSet;

import java.util.List;

/**
 * @author Christopher Beckmann
 * @version 2.0
 * @since 05.09.2017
 * created 05.09.2017
 */
public class ExerciseSetSpinnerAdapter extends ArrayAdapter<ExerciseSet> {

    private int resource;
    private List<ExerciseSet> sets;

    public ExerciseSetSpinnerAdapter(@NonNull Context context, @LayoutRes int resource, List<ExerciseSet> data) {
        super(context, resource, data);
        this.resource = resource;
        this.sets = data;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View cv, ViewGroup parent) {

        final ExerciseSet set = sets.get(position);

        View row = null;
        if(cv == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(resource, parent, false);
        } else {
            row = cv;
        }

        if(set == null) return row;

        CardView card = (CardView) row.findViewById(R.id.exercise_set_card);
        TextView name = (TextView) row.findViewById(R.id.exercise_set_name);
        LinearLayout exerciseList = (LinearLayout) row.findViewById(R.id.exercise_list);
        TextView noExercisesText = (TextView) row.findViewById(R.id.exercise_none_available);

        card.setClickable(false);
        card.setLongClickable(false);

        name.setText(set.getName());

        exerciseList.removeAllViews();

        for(int i = 0; i < set.size(); ++i) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_round_exercise_image, null, false);
            ImageView image = (ImageView) view.findViewById(R.id.exercise_image);

            Glide.with(getContext()).load(set.get(i).getImageResIds(getContext())[0]).into(image);

            exerciseList.addView(view);
        }

        if(set.size() == 0) {
            noExercisesText.setVisibility(View.VISIBLE);
        } else {
            noExercisesText.setVisibility(View.GONE);
        }

        return row;
    }

    public void updateData(@NonNull List<ExerciseSet> data) {
        this.sets = data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.sets.size();
    }


    @Override
    public ExerciseSet getItem(int location) {
        return this.sets.get(location);
    }


    @Override
    public long getItemId(int position) {
        return sets.get(position).getId();
    }
}
