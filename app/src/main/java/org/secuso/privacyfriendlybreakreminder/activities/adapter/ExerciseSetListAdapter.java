package org.secuso.privacyfriendlybreakreminder.activities.adapter;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.EditExerciseSetActivity;
import org.secuso.privacyfriendlybreakreminder.activities.ManageExerciseSetsActivity;
import org.secuso.privacyfriendlybreakreminder.activities.tutorial.FirstLaunchManager;
import org.secuso.privacyfriendlybreakreminder.database.data.Exercise;
import org.secuso.privacyfriendlybreakreminder.database.data.ExerciseSet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


/**
 * @author Christopher Beckmann
 * @version 2.0
 * @since 04.09.2017
 * created 04.09.2017
 */
public class ExerciseSetListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    List<ExerciseSet> data = new LinkedList<ExerciseSet>();
    List<Long> deleteIds = new LinkedList<>();
    ManageExerciseSetsActivity mContext = null;

    private boolean deleteMode = false;

    public ExerciseSetListAdapter(ManageExerciseSetsActivity context, List<ExerciseSet> data) {
        if(context == null) throw new IllegalArgumentException("Context may not be null");

        mContext = context;

        setData(data);

        setHasStableIds(true);
    }

    public void setData(List<ExerciseSet> data) {
        if(data != null) {
            this.data = data;

            boolean hideDefaultSets = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(FirstLaunchManager.PREF_HIDE_DEFAULT_SETS, false);
            if(hideDefaultSets) {

                Iterator<ExerciseSet> iter = this.data.iterator();
                while(iter.hasNext()) {
                    ExerciseSet set = iter.next();

                    if(set.isDefaultSet()) iter.remove();
                }
            }
        }

        notifyDataSetChanged();
    }

    public void enableDeleteMode() {
        deleteMode = true;
        notifyDataSetChanged();
    }

    public void disableDeleteMode() {
        deleteMode = false;
        deleteIds.clear();
        notifyDataSetChanged();
    }

    public List<Long> getDeleteIdList() {
        return deleteIds;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ExerciseSetViewHolder vh = (ExerciseSetViewHolder) holder;

        final ExerciseSet set = data.get(position);

        vh.deleteCheckBox.setVisibility((deleteMode && !set.isDefaultSet()) ? View.VISIBLE : View.GONE);
        vh.deleteCheckBox.setChecked(false);

        vh.name.setText(set.getName());
        vh.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(deleteMode) {

                    if(set.isDefaultSet()) {
                        Toast.makeText(mContext, R.string.exercise_set_can_not_be_deleted, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(vh.deleteCheckBox.isChecked()) {
                        deleteIds.remove(set.getId());
                        vh.deleteCheckBox.setChecked(false);
                    } else {
                        if (!deleteIds.contains(set.getId())) deleteIds.add(set.getId());
                        vh.deleteCheckBox.setChecked(true);
                    }

                } else {
                    if(set.isDefaultSet()) {
                        Toast.makeText(mContext, R.string.exercise_set_can_not_be_edited, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent i = new Intent(mContext, EditExerciseSetActivity.class);
                    i.putExtra(EditExerciseSetActivity.EXTRA_EXERCISE_SET_ID, set.getId());
                    i.putExtra(EditExerciseSetActivity.EXTRA_EXERCISE_SET_NAME, set.getName());
                    mContext.startActivity(i);
                }
            }
        });
        vh.card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(deleteMode) {

                    if(set.isDefaultSet()) {
                        Toast.makeText(mContext, R.string.exercise_set_can_not_be_deleted, Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    if(vh.deleteCheckBox.isChecked()) {
                        deleteIds.remove(set.getId());
                        vh.deleteCheckBox.setChecked(false);
                    } else {
                        if (!deleteIds.contains(set.getId())) deleteIds.add(set.getId());
                        vh.deleteCheckBox.setChecked(true);
                    }

                } else {
                    mContext.enableDeleteMode();
                }
                return false;
            }
        });

        vh.exerciseList.removeAllViews();

        for(int i = 0; i < set.size(); ++i) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_round_exercise_image, null, false);
            ImageView image = (ImageView) view.findViewById(R.id.exercise_image);

            Glide.with(mContext).load(set.get(i).getImageResIds(mContext)[0]).transition(DrawableTransitionOptions.withCrossFade()).into(image);

            vh.exerciseList.addView(view);
        }

        if(set.size() == 0) {
            vh.noExercisesText.setVisibility(View.VISIBLE);
            vh.exerciseTime.setVisibility(View.GONE);
        } else {
            vh.noExercisesText.setVisibility(View.GONE);
            vh.exerciseTime.setVisibility(View.VISIBLE);

            int seconds = (int) set.getExerciseSetTime(mContext);
            vh.exerciseTime.setText(String.format(Locale.getDefault(), "%02d:%02d", (seconds / 60), (seconds % 60)));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_exercise_set, parent, false);
        return new ExerciseSetViewHolder(itemView);
    }

    @Override
    public long getItemId(int position) {
        if(data != null) {
            return data.get(position).getId();
        }
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        if(data != null) {
            return data.size();
        }
        return 0;
    }

    public class ExerciseSetViewHolder extends RecyclerView.ViewHolder {

        LinearLayout exerciseList;
        TextView name;
        CardView card;
        TextView noExercisesText;
        CheckBox deleteCheckBox;
        TextView exerciseTime;

        public ExerciseSetViewHolder(View itemView) {
            super(itemView);

            card = (CardView) itemView.findViewById(R.id.exercise_set_card);
            name = (TextView) itemView.findViewById(R.id.exercise_set_name);
            exerciseList = (LinearLayout) itemView.findViewById(R.id.exercise_list);
            noExercisesText = (TextView) itemView.findViewById(R.id.exercise_none_available);
            deleteCheckBox = (CheckBox) itemView.findViewById(R.id.delete_check_box);
            exerciseTime = (TextView) itemView.findViewById(R.id.exercise_set_time_short);
        }
    }
}
