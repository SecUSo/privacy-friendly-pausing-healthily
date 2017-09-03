package org.secuso.privacyfriendlybreakreminder.activities.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.helper.CursorRecyclerViewAdapter;
import org.secuso.privacyfriendlybreakreminder.database.columns.ExerciseColumns;
import org.secuso.privacyfriendlybreakreminder.database.data.Exercise;

/**
 * Created by Christopher Beckmann on 30.08.2017.
 */
public class ExerciseAdapter extends CursorRecyclerViewAdapter<ViewHolder> {


    public ExerciseAdapter(Context context, Cursor cursor) {
        super(context, cursor, ExerciseColumns._ID);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_exercise, parent, false);
        return new ExerciseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        final Exercise exercise = ExerciseColumns.fromCursor(cursor);

        ExerciseViewHolder vh = (ExerciseViewHolder) viewHolder;

        String imageID = exercise.getImageID();
        String[] imageIDSplit = imageID.split(",");

        if(imageIDSplit.length > 1) {
            imageID = imageIDSplit[0]; // only take the first image as a display image
        }

        int imageResID = mContext.getResources().getIdentifier(
                "exercise_" + imageID,
                "drawable",
                mContext.getPackageName());
        vh.image.setImageResource(imageResID);
        vh.name.setText(exercise.getName());
        vh.executionText.setText(exercise.getExecution());
        vh.descriptionText.setText(exercise.getDescription());
        vh.section.setText(exercise.getSection());
    }

    public class ExerciseViewHolder extends ViewHolder {

        ImageView image;
        TextView name;
        TextView executionText;
        TextView descriptionText;
        TextView section;

        public ExerciseViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.exercise_name);
            image = (ImageView) itemView.findViewById(R.id.exercise_image);
            executionText = (TextView) itemView.findViewById(R.id.exercise_execution);
            descriptionText = (TextView) itemView.findViewById(R.id.exercise_description);
            section = (TextView) itemView.findViewById(R.id.exercise_section);

        }
    }
}
