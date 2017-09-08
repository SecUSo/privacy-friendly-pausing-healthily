package org.secuso.privacyfriendlybreakreminder.activities.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.database.data.Exercise;
import org.secuso.privacyfriendlybreakreminder.database.data.ExerciseSet;

/**
 * This adapter provides the {@link Exercise}s of one {@link ExerciseSet}.
 * Use {@link ExerciseSetAdapter#add(Exercise)} and {@link ExerciseSetAdapter#remove(Exercise)} to manage the {@link Exercise}s.
 * @author Christopher Beckmann
 * @see android.support.v7.widget.RecyclerView.Adapter
 */
public class ExerciseSetAdapter extends RecyclerView.Adapter<ViewHolder> {

    private ExerciseSet set;
    private Context mContext;

    public ExerciseSetAdapter(Context context, ExerciseSet set) {
        this.mContext = context;
        this.set = set;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_exercise, parent, false);
        return new ExerciseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(set == null) return;

        final Exercise exercise = set.get(position);

        ExerciseViewHolder vh = (ExerciseViewHolder) holder;

        vh.image.setImageResource(exercise.getImageResIds(mContext)[0]);
        vh.executionText.setText(exercise.getExecution());
        vh.descriptionText.setText(exercise.getDescription());
        vh.section.setText(exercise.getSection());
    }

    @Override
    public int getItemCount() {
        if(set != null)
            return set.size();
        else
            return 0;
    }

    public void updateData(ExerciseSet set) {
        this.set = set;
        notifyDataSetChanged();
    }

    public void add(Exercise e) {
        if(set.add(e)) notifyItemInserted(set.size()-1);
    }

    public void remove(Exercise e) {
        int index = set.indexOf(e);
        if(set.remove(e)) notifyItemRemoved(index);
    }

    public ExerciseSet getExerciseSet() {
        return set;
    }

    public class ExerciseViewHolder extends ViewHolder {

        ImageView image;
        TextView executionText;
        TextView descriptionText;
        TextView section;

        public ExerciseViewHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.exercise_image);
            executionText = (TextView) itemView.findViewById(R.id.execution);
            descriptionText = (TextView) itemView.findViewById(R.id.description);
            section = (TextView) itemView.findViewById(R.id.section);
        }
    }
}
