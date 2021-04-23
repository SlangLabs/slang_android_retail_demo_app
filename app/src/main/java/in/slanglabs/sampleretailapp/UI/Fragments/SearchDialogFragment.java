package in.slanglabs.sampleretailapp.UI.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import in.slanglabs.sampleretailapp.Model.ItemOfferCart;
import in.slanglabs.sampleretailapp.Model.ListType;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.UI.ViewModel.AppViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchDialogFragment extends DialogFragment {

    private static final String TAG = "AutoCompleteDialogFragment";
    public ViewItemListener viewItemListener;
    private EditText filterText;
    private NameAdapter nameAdapter;
    private ImageView searchClearButton;
    private AppViewModel appViewModel;
    private String searchString;
    private TextView searchViewHeader;

    public static SearchDialogFragment newInstance(String searchText) {
        SearchDialogFragment myFragment = new SearchDialogFragment();
        Bundle args = new Bundle();
        args.putString("searchString", searchText);
        myFragment.setArguments(args);
        return myFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) dismiss();
        setStyle(DialogFragment.STYLE_NORMAL, R.style.full_screen_dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = inflater.inflate(R.layout.autocomplete_dialog_fragment, container,
                false);
        if (getArguments() != null) {
            searchString = getArguments().getString("searchString", "");
        }
        filterText = view.findViewById(R.id.search_text);
        searchViewHeader = view.findViewById(R.id.search_items_header);
        if (filterText.requestFocus()) {
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        searchClearButton = view.findViewById(R.id.clear_text);
        searchClearButton.setOnClickListener(view1 -> {
            filterText.getText().clear();
            InputMethodManager imm = (InputMethodManager) view1.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
        });

        filterText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewItemListener.onItemClicked(textView.getText().toString());
                dismiss();
                return true;
            }
            return false;
        });
        RecyclerView recyclerView = view.findViewById(R.id.list_item_view);
        if (savedInstanceState != null) {
            dismiss();
        }
        nameAdapter = new NameAdapter(getContext(),
                R.layout.autocomplete_list_item);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(nameAdapter);
        ImageButton cancelButton = view.findViewById(R.id.back_arrow);
        cancelButton.setOnClickListener(v -> dismiss());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        appViewModel = new ViewModelProvider(this).get(
                AppViewModel.class);
        appViewModel.getSearchForNameMediator().observe(
                getViewLifecycleOwner(), items -> nameAdapter.setItems(items));
        filterText.addTextChangedListener(filterTextWatcher);
        filterText.setText(searchString);
        filterText.setSelection(filterText.getText().length());
        if (appViewModel.getListType().getValue() != null) {
            searchString = getArguments() != null ? getArguments()
                    .getString("searchString", "") : "";
            @ListType String listType = appViewModel.getListType().getValue();
            switch (listType) {
                case ListType.GROCERY:
                    searchViewHeader.setText("Search Grocery Items");
                    break;
                case ListType.PHARMACY:
                    searchViewHeader.setText("Search Pharmacy Items");
                    break;
                case ListType.FASHION:
                    searchViewHeader.setText("Search Fashion Items");
                    break;
            }
        }
    }

    private TextWatcher filterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            String currentSearchTerm = s.toString();
            appViewModel.getSearchItem(currentSearchTerm);
            searchClearButton.setVisibility(View.VISIBLE);
        }
    };

    public interface ViewItemListener {
        public void onItemClicked(String item);
    }

    public class NameHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        AppCompatTextView countryName;

        public NameHolder(Context context, @NonNull View itemView) {
            super(itemView);
            this.countryName = itemView.findViewById(R.id.auto_complete_title);
            itemView.setOnClickListener(this);
        }

        public void setName(String name) {
            this.countryName.setText(name);
        }

        @Override
        public void onClick(View view) {
            viewItemListener.onItemClicked(countryName.getText().toString());
            SearchDialogFragment.this.dismiss();
        }
    }

    public class NameAdapter extends RecyclerView.Adapter<NameHolder> {

        void setItems(List<ItemOfferCart> items) {
            this.items.clear();
            this.items.addAll(items);
            notifyDataSetChanged();
        }

        private final ArrayList<ItemOfferCart> items = new ArrayList<>();

        private final Context context;
        private final int itemResource;

        NameAdapter(Context context, int itemResource) {
            this.context = context;
            this.itemResource = itemResource;
        }

        @NonNull
        @Override
        public NameHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                             int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(this.itemResource, parent, false);
            return new NameHolder(this.context, view);
        }

        @Override
        public void onBindViewHolder(@NonNull NameHolder holder, int position) {
            String name = items.get(position).item.name;
            holder.setName(name);
        }

        @Override
        public int getItemCount() {
            return this.items.size();
        }
    }
}
