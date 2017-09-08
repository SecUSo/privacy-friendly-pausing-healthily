package org.secuso.privacyfriendlybreakreminder.activities.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlybreakreminder.R;
import org.secuso.privacyfriendlybreakreminder.activities.ChooseExerciseActivity;
import org.secuso.privacyfriendlybreakreminder.database.data.Exercise;
import org.secuso.privacyfriendlybreakreminder.database.data.ExerciseSet;
import org.secuso.privacyfriendlybreakreminder.dialog.ExerciseDialog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Christopher Beckmann on 06.09.2017.
 */

public class ExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Integer> checkedIds = new ArrayList<>();
    private boolean mShowCheckboxes = false;

    public static final Comparator<Exercise> ID_COMPARATOR = new Comparator<Exercise>() {
        @Override
        public int compare(Exercise a, Exercise b) {
            return (a.getId() < b.getId()) ? -1 : ((a.getId() == b.getId()) ? 0 : 1);
        }
    };

    private final LayoutInflater mInflater;
    private final Comparator<Exercise> mComparator;
    private final SortedList<Exercise> mSortedList = new SortedList<>(Exercise.class, new SortedList.Callback<Exercise>() {
        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public int compare(Exercise a, Exercise b) {
            return mComparator.compare(a, b);
        }

        @Override
        public boolean areContentsTheSame(Exercise oldItem, Exercise newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(Exercise item1, Exercise item2) {
            return item1.getId() == item2.getId();
        }
    });

    public ExerciseAdapter(Context context, Comparator<Exercise> comparator) {
        this.mContext = context;
        this.mComparator = comparator;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.layout_exercise_grid_item, parent, false);
        return new ExerciseAdapter.ExerciseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Exercise exercise = mSortedList.get(position);

        final ExerciseViewHolder vh = (ExerciseViewHolder) holder;

        final View.OnClickListener infoClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExerciseDialog.showExerciseDialog(mContext, exercise);
            }
        };

        final View.OnClickListener checkClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vh.checkbox.isChecked()) {
                    checkedIds.remove(checkedIds.indexOf(exercise.getId()));
                } else {
                    checkedIds.add(exercise.getId());
                }

                vh.checkbox.setChecked(!vh.checkbox.isChecked());
            }
        };

        vh.image.setImageResource(exercise.getImageResIds(mContext)[0]);

        if(checkedIds != null)
            vh.checkbox.setChecked(checkedIds.contains(exercise.getId()));

        vh.checkbox.setClickable(false);
        vh.checkbox.setVisibility(mShowCheckboxes ? View.VISIBLE : View.GONE);

        vh.layout.setOnClickListener(mShowCheckboxes ? checkClick : infoClick);
        vh.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                infoClick.onClick(v);
                return true;
            }
        });

        vh.infoButton.setOnClickListener(infoClick);
    }

    @Override
    public int getItemCount() {
        return mSortedList.size();
    }

    public void replaceAll(@NonNull List<Exercise> exercises) {
        mSortedList.beginBatchedUpdates();
        for (int i = mSortedList.size() - 1; i >= 0; i--) {
            final Exercise ex = mSortedList.get(i);
            if (!exercises.contains(ex)) {
                mSortedList.remove(ex);
            }
        }
        mSortedList.addAll(exercises);
        mSortedList.endBatchedUpdates();
    }

    public List<Integer> getCheckedIds() {
        return checkedIds;
    }

    public void add(Exercise model) {
        mSortedList.add(model);
    }

    public void remove(Exercise model) {
        mSortedList.remove(model);
    }

    public void add(List<Exercise> models) {
        mSortedList.addAll(models);
    }

    public void remove(List<Exercise> models) {
        mSortedList.beginBatchedUpdates();
        for (Exercise model : models) {
            mSortedList.remove(model);
        }
        mSortedList.endBatchedUpdates();
    }

    public void setCheckedItems(@NonNull List<Integer> checkedItems) {
        this.checkedIds = checkedItems;
    }

    public List<Exercise> getExercises() {
        List<Exercise> result = new LinkedList<>();

        for (int i = 0; i < mSortedList.size(); ++i) {
            Exercise e = mSortedList.get(i);
            result.add(e);
        }

        return result;
    }

    public class ExerciseViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        CheckBox checkbox;
        ImageButton infoButton;
        ConstraintLayout layout;

        public ExerciseViewHolder(View itemView) {
            super(itemView);

            layout = (ConstraintLayout) itemView.findViewById(R.id.exercise_layout);
            checkbox = (CheckBox) itemView.findViewById(R.id.exercise_checkbox);
            image = (ImageView) itemView.findViewById(R.id.exercise_image);
            infoButton = (ImageButton) itemView.findViewById(R.id.exercise_info_button);
        }
    }

    public void showCheckboxes(boolean show) {
        mShowCheckboxes = show;
    }
}
