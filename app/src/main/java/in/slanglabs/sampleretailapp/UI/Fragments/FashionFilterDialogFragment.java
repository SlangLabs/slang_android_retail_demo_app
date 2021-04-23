package in.slanglabs.sampleretailapp.UI.Fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import in.slanglabs.sampleretailapp.Model.FilterOptions;
import in.slanglabs.sampleretailapp.Model.PriceRange;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.UI.ViewModel.AppViewModel;

public class FashionFilterDialogFragment extends DialogFragment {

    private static final String TAG = "FashionFilterDialogFragment";
    public GroceryAndPharmaFilterDialogFragment.ViewItemListener viewItemListener;
    private AppViewModel appViewModel;

    private List<String> brands = new ArrayList<>();
    private List<String> colors = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private List<String> genders = new ArrayList<>();
    private List<Boolean> selectedBrands = new ArrayList<>();
    private List<Boolean> selectedColors = new ArrayList<>();
    private List<Boolean> selectedCategories = new ArrayList<>();
    private List<Boolean> selectedGenders = new ArrayList<>();

    private List<PriceRange> prices = Arrays.asList(
            new PriceRange(500, 800),
            new PriceRange(801, 1200),
            new PriceRange(1201, 1500),
            new PriceRange(1501, 1800),
            new PriceRange(1801, -1));
    private String[] priceRangesStringArray;
    private List<Boolean> selectedPrices = new ArrayList<>();

    private List<String> sizes = new ArrayList<>();
    private List<Boolean> selectedSizes = new ArrayList<>();

    private FilterOptions filterOptions;

    TextView brandSelectedTextView;
    TextView colorsSelectedTextView;
    TextView unitsSelectedTextView;
    TextView variantsSelectedTextView;
    TextView categoriesSelectedTextView;
    TextView genderSelectedTextView;

    public interface ViewItemListener {
        public void onFilterChanged(FilterOptions filterOptions);
    }

    public static FashionFilterDialogFragment newInstance(FilterOptions filterOptions) {
        FashionFilterDialogFragment myFragment = new FashionFilterDialogFragment();
        myFragment.filterOptions = new FilterOptions();
        Bundle args = new Bundle();
        args.putParcelable("filterOptions", filterOptions);
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
        if (savedInstanceState != null) {
            dismiss();
        }
        View view = inflater.inflate(R.layout.filter_layout, container,
                false);

        brandSelectedTextView = view.findViewById(R.id.brand_selected_text);
        categoriesSelectedTextView = view.findViewById(R.id.category_selected_text);
        colorsSelectedTextView = view.findViewById(R.id.colors_selected_text);
        unitsSelectedTextView = view.findViewById(R.id.units_selected_text);
        variantsSelectedTextView = view.findViewById(R.id.variant_selected_text);
        genderSelectedTextView = view.findViewById(R.id.gender_selected_text);

        View brandView = view.findViewById(R.id.item_brand_view);
        brandView.setOnClickListener(view1 -> {
            final boolean[] selectedColorFiltersArray = new boolean[selectedBrands.size()];
            for (int i = 0; i < selectedBrands.size(); i++) {
                selectedColorFiltersArray[i] = selectedBrands.get(i);
            }
            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
            mBuilder.setTitle("Select preferred filter:");
            mBuilder.setMultiChoiceItems(brands.toArray(new String[0]),
                    selectedColorFiltersArray, (dialog, position, isChecked) -> {
                        selectedBrands.set(position, isChecked);
                    });
            mBuilder.setPositiveButton("OK", (dialog, which) -> {
                List<String> finalColorFilters = new ArrayList<>();
                for (int i = 0; i < selectedBrands.size(); i++) {
                    if (selectedBrands.get(i)) {
                        finalColorFilters.add(brands.get(i));
                    }
                }
                filterOptions.setBrands(finalColorFilters);
                int totalCount = filterOptions.getBrands().size();
                if (totalCount > 0) {
                    brandSelectedTextView.setVisibility(View.VISIBLE);
                    brandSelectedTextView.setText(String.format(Locale.ENGLISH,
                            "%d Selected", filterOptions.getBrands().size()));
                } else {
                    brandSelectedTextView.setVisibility(View.GONE);
                }
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        });

        View variantsView = view.findViewById(R.id.item_price_view);
        variantsView.setOnClickListener(view1 -> {
            final boolean[] selectedVariantsFiltersArray = new boolean[selectedPrices.size()];
            for (int i = 0; i < selectedPrices.size(); i++) {
                selectedVariantsFiltersArray[i] = selectedPrices.get(i);
            }
            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
            mBuilder.setTitle("Select preferred filter:");
            mBuilder.setMultiChoiceItems(priceRangesStringArray,
                    selectedVariantsFiltersArray, (dialog, position, isChecked) -> {
                        selectedPrices.set(position, isChecked);
                    });
            mBuilder.setPositiveButton("OK", (dialog, which) -> {
                List<PriceRange> finalVariantFilters = new ArrayList<>();
                for (int i = 0; i < selectedPrices.size(); i++) {
                    if (selectedPrices.get(i)) {
                        finalVariantFilters.add(prices.get(i));
                    }
                }
                filterOptions.setPriceRanges(finalVariantFilters);
                int totalCount = filterOptions.getPriceRanges().size();
                if (totalCount > 0) {
                    variantsSelectedTextView.setVisibility(View.VISIBLE);
                    variantsSelectedTextView.setText(String.format(Locale.ENGLISH,
                            "%d Selected", filterOptions.getPriceRanges().size()));
                } else {
                    variantsSelectedTextView.setVisibility(View.GONE);
                }
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        });

        View unitsView = view.findViewById(R.id.item_units_view);
        unitsView.setOnClickListener(view1 -> {
            final boolean[] selectedUnitsFiltersArray = new boolean[selectedSizes.size()];
            for (int i = 0; i < selectedSizes.size(); i++) {
                selectedUnitsFiltersArray[i] = selectedSizes.get(i);
            }
            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
            mBuilder.setTitle("Select preferred filter:");
            mBuilder.setMultiChoiceItems(sizes.toArray(new String[0]),
                    selectedUnitsFiltersArray, (dialog, position, isChecked) -> {
                        selectedSizes.set(position, isChecked);
                    });
            mBuilder.setPositiveButton("OK", (dialog, which) -> {
                List<String> finalUnitsFilters = new ArrayList<>();
                for (int i = 0; i < selectedSizes.size(); i++) {
                    if (selectedSizes.get(i)) {
                        finalUnitsFilters.add(sizes.get(i));
                    }
                }
                filterOptions.setSizes(finalUnitsFilters);
                int totalCount = filterOptions.getSizes().size();
                if (totalCount > 0) {
                    unitsSelectedTextView.setVisibility(View.VISIBLE);
                    unitsSelectedTextView.setText(String.format(Locale.ENGLISH,
                            "%d Selected", filterOptions.getSizes().size()));
                } else {
                    unitsSelectedTextView.setVisibility(View.GONE);
                }
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        });

        View colorsView = view.findViewById(R.id.item_colors_view);
        colorsView.setOnClickListener(view1 -> {
            final boolean[] selectedUnitsFiltersArray = new boolean[selectedColors.size()];
            for (int i = 0; i < selectedColors.size(); i++) {
                selectedUnitsFiltersArray[i] = selectedColors.get(i);
            }
            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
            mBuilder.setTitle("Select preferred filter:");
            mBuilder.setMultiChoiceItems(colors.toArray(new String[0]),
                    selectedUnitsFiltersArray, (dialog, position, isChecked) -> {
                        selectedColors.set(position, isChecked);
                    });
            mBuilder.setPositiveButton("OK", (dialog, which) -> {
                List<String> finalUnitsFilters = new ArrayList<>();
                for (int i = 0; i < selectedColors.size(); i++) {
                    if (selectedColors.get(i)) {
                        finalUnitsFilters.add(colors.get(i));
                    }
                }
                filterOptions.setColors(finalUnitsFilters);
                int totalCount = filterOptions.getColors().size();
                if (totalCount > 0) {
                    colorsSelectedTextView.setVisibility(View.VISIBLE);
                    colorsSelectedTextView.setText(String.format(Locale.ENGLISH,
                            "%d Selected", filterOptions.getColors().size()));
                } else {
                    colorsSelectedTextView.setVisibility(View.GONE);
                }
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        });

        View categoriesView = view.findViewById(R.id.item_category_view);
        categoriesView.setOnClickListener(view1 -> {
            final boolean[] selectedUnitsFiltersArray = new boolean[selectedCategories.size()];
            for (int i = 0; i < selectedCategories.size(); i++) {
                selectedUnitsFiltersArray[i] = selectedCategories.get(i);
            }
            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
            mBuilder.setTitle("Select preferred filter:");
            mBuilder.setMultiChoiceItems(categories.toArray(new String[0]),
                    selectedUnitsFiltersArray, (dialog, position, isChecked) -> {
                        selectedCategories.set(position, isChecked);
                    });
            mBuilder.setPositiveButton("OK", (dialog, which) -> {
                List<String> finalUnitsFilters = new ArrayList<>();
                for (int i = 0; i < selectedCategories.size(); i++) {
                    if (selectedCategories.get(i)) {
                        finalUnitsFilters.add(categories.get(i));
                    }
                }
                filterOptions.setCategories(finalUnitsFilters);
                int totalCount = filterOptions.getCategories().size();
                if (totalCount > 0) {
                    categoriesSelectedTextView.setVisibility(View.VISIBLE);
                    categoriesSelectedTextView.setText(String.format(Locale.ENGLISH,
                            "%d Selected", filterOptions.getCategories().size()));
                } else {
                    categoriesSelectedTextView.setVisibility(View.GONE);
                }
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        });

        View gendersView = view.findViewById(R.id.item_gender_view);
        gendersView.setOnClickListener(view1 -> {
            final boolean[] selectedUnitsFiltersArray = new boolean[selectedGenders.size()];
            for (int i = 0; i < selectedGenders.size(); i++) {
                selectedUnitsFiltersArray[i] = selectedGenders.get(i);
            }
            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
            mBuilder.setTitle("Select preferred filter:");
            mBuilder.setMultiChoiceItems(genders.toArray(new String[0]),
                    selectedUnitsFiltersArray, (dialog, position, isChecked) -> {
                        selectedGenders.set(position, isChecked);
                    });
            mBuilder.setPositiveButton("OK", (dialog, which) -> {
                List<String> finalUnitsFilters = new ArrayList<>();
                for (int i = 0; i < selectedGenders.size(); i++) {
                    if (selectedGenders.get(i)) {
                        finalUnitsFilters.add(genders.get(i));
                    }
                }
                filterOptions.setGenders(finalUnitsFilters);
                int totalCount = filterOptions.getGenders().size();
                if (totalCount > 0) {
                    genderSelectedTextView.setVisibility(View.VISIBLE);
                    genderSelectedTextView.setText(String.format(Locale.ENGLISH,
                            "%d Selected", filterOptions.getGenders().size()));
                } else {
                    genderSelectedTextView.setVisibility(View.GONE);
                }
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        });

        View applyView = view.findViewById(R.id.filter_apply_button);
        applyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewItemListener.onFilterChanged(filterOptions);
                dismiss();
            }
        });

        View resetView = view.findViewById(R.id.filter_reset_button);
        resetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterOptions.clear();
                viewItemListener.onFilterChanged(filterOptions);
                dismiss();
            }
        });

        ImageButton cancelButton = view.findViewById(R.id.back_arrow);
        cancelButton.setOnClickListener(v -> {
            dismiss();
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        appViewModel = new ViewModelProvider(this).get(
                AppViewModel.class);
        if (getArguments() != null) {
            filterOptions = getArguments().getParcelable("filterOptions");
        }

        appViewModel.getItemBrands(appViewModel.getListType().getValue()).observe(this.getViewLifecycleOwner(),
                brands -> {
                    FashionFilterDialogFragment.this.brands.addAll(brands);
                    for (String brand : brands) {
                        selectedBrands.add(false);
                    }
                    if (filterOptions != null) {
                        for (String color : filterOptions.getBrands()) {
                            int index = brands.indexOf(color);
                            selectedBrands.set(index, true);
                        }
                        if (filterOptions.getBrands().size() > 0) {
                            brandSelectedTextView.setVisibility(View.VISIBLE);
                            brandSelectedTextView.setText(String.format(Locale.ENGLISH,
                                    "%d Selected", filterOptions.getBrands().size()));
                        } else {
                            brandSelectedTextView.setVisibility(View.GONE);
                        }
                    }
                });

        appViewModel.getItemSizes(appViewModel.getListType().getValue()).observe(this.getViewLifecycleOwner(),
                sizes -> {
                    FashionFilterDialogFragment.this.sizes.addAll(sizes);
                    for (String size : sizes) {
                        selectedSizes.add(false);
                    }
                    if (filterOptions != null) {
                        for (String unit : filterOptions.getSizes()) {
                            int index = sizes.indexOf(unit);
                            selectedSizes.set(index, true);
                        }
                        if (filterOptions.getSizes().size() > 0) {
                            unitsSelectedTextView.setVisibility(View.VISIBLE);
                            unitsSelectedTextView.setText(String.format(Locale.ENGLISH,
                                    "%d Selected", filterOptions.getSizes().size()));
                        } else {
                            unitsSelectedTextView.setVisibility(View.GONE);
                        }
                    }
                });

        appViewModel.getItemColors(appViewModel.getListType().getValue()).observe(this.getViewLifecycleOwner(),
                colors -> {
                    FashionFilterDialogFragment.this.colors.addAll(colors);
                    for (String color : colors) {
                        selectedColors.add(false);
                    }
                    if (filterOptions != null) {
                        for (String color : filterOptions.getColors()) {
                            int index = colors.indexOf(color);
                            selectedColors.set(index, true);
                        }
                        if (filterOptions.getColors().size() > 0) {
                            colorsSelectedTextView.setVisibility(View.VISIBLE);
                            colorsSelectedTextView.setText(String.format(Locale.ENGLISH,
                                    "%d Selected", filterOptions.getColors().size()));
                        } else {
                            colorsSelectedTextView.setVisibility(View.GONE);
                        }
                    }
                });

        appViewModel.getItemCategories(appViewModel.getListType().getValue()).observe(this.getViewLifecycleOwner(),
                categories -> {
                    FashionFilterDialogFragment.this.categories.addAll(categories);
                    for (String category : categories) {
                        selectedCategories.add(false);
                    }
                    if (filterOptions != null) {
                        for (String category : filterOptions.getCategories()) {
                            int index = categories.indexOf(category);
                            selectedCategories.set(index, true);
                        }
                        if (filterOptions.getCategories().size() > 0) {
                            categoriesSelectedTextView.setVisibility(View.VISIBLE);
                            categoriesSelectedTextView.setText(String.format(Locale.ENGLISH,
                                    "%d Selected", filterOptions.getCategories().size()));
                        } else {
                            categoriesSelectedTextView.setVisibility(View.GONE);
                        }
                    }
                });

        appViewModel.getItemGenders(appViewModel.getListType().getValue()).observe(this.getViewLifecycleOwner(),
                genders -> {
                    FashionFilterDialogFragment.this.genders.addAll(genders);
                    for (String gender : genders) {
                        selectedGenders.add(false);
                    }
                    if (filterOptions != null) {
                        for (String gender : filterOptions.getGenders()) {
                            int index = genders.indexOf(gender);
                            selectedGenders.set(index, true);
                        }
                        if (filterOptions.getGenders().size() > 0) {
                            genderSelectedTextView.setVisibility(View.VISIBLE);
                            genderSelectedTextView.setText(String.format(Locale.ENGLISH,
                                    "%d Selected", filterOptions.getGenders().size()));
                        } else {
                            genderSelectedTextView.setVisibility(View.GONE);
                        }
                    }
                });


        priceRangesStringArray = new String[prices.size()];
        for (int i = 0; i < prices.size(); i ++) {
            PriceRange priceRange = prices.get(i);
            int stopPrice = priceRange.getStopPrice();
            String stopPriceString = priceRange.getStopPrice()+"";
            if(priceRange.getStopPrice() == -1) {
                stopPriceString = "above";
            }
            String priceString = priceRange.getStartPrice() + " - " + stopPriceString;
            priceRangesStringArray[i] = priceString;
        }

        if (filterOptions != null) {
            for (PriceRange variant : prices) {
                selectedPrices.add(false);
            }
            for (PriceRange variant : filterOptions.getPriceRanges()) {
                int index = prices.indexOf(variant);
                selectedPrices.set(index, true);
            }
            if (filterOptions.getPriceRanges().size() > 0) {
                variantsSelectedTextView.setVisibility(View.VISIBLE);
                variantsSelectedTextView.setText(String.format(Locale.ENGLISH,
                        "%d Selected", filterOptions.getPriceRanges().size()));
            } else {
                variantsSelectedTextView.setVisibility(View.GONE);
            }
        }
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
}


