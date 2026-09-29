package com.keith.foodtracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int BG = Color.rgb(16, 18, 22);
    private static final int CARD = Color.rgb(30, 34, 41);
    private static final int CARD_2 = Color.rgb(39, 44, 53);
    private static final int TEXT = Color.rgb(245, 247, 250);
    private static final int MUTED = Color.rgb(168, 176, 188);
    private static final int ACCENT = Color.rgb(77, 166, 255);
    private static final int PROTEIN = Color.rgb(74, 185, 255);
    private static final int CARBS = Color.rgb(94, 214, 149);
    private static final int FAT = Color.rgb(255, 182, 84);
    private static final int DANGER = Color.rgb(224, 88, 88);

    private static final double TARGET_KCAL = 1886.0;
    private static final double TARGET_PROTEIN = 168.7;
    private static final double TARGET_CARBS = 205.5;
    private static final double TARGET_FAT = 41.3;
    private static final double TARGET_FIBRE = 38.2;

    private final DecimalFormat oneDp = new DecimalFormat("0.#");
    private final List<Food> foods = new ArrayList<>();
    private final List<Entry> todayEntries = new ArrayList<>();

    private android.content.SharedPreferences prefs;
    private FrameLayout content;
    private Button todayTab;
    private Button historyTab;

    private Spinner foodSpinner;
    private EditText amountInput;
    private TextView unitLabel;
    private Button logButton;
    private LinearLayout loggedList;
    private TextView kcalTotal;
    private TextView proteinTotal;
    private TextView carbsTotal;
    private TextView fatTotal;
    private TextView fibreTotal;
    private TextView macroLegend;
    private MacroPieView pieView;
    private int editingIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        prefs = getSharedPreferences("food_tracker_data", MODE_PRIVATE);
        seedFoods();
        buildShell();
        showToday();
    }

    private void seedFoods() {
        // Nutrient figures exactly reproduce the checked fixed plan totals:
        // 1,886 kcal, 168.7 g protein, 205.5 g carbs, 41.3 g fat, 38.2 g fibre.
        foods.add(new Food("oats", "Oats", 90, "g", 333, 11.3, 54.9, 7.0, 8.2));
        foods.add(new Food("fruit", "Frozen fruit", 100, "g", 41, 1.0, 8.4, 0.2, 2.9));
        foods.add(new Food("milk_breakfast", "Skimmed milk", 200, "ml", 70, 7.0, 10.0, 0.2, 0));
        foods.add(new Food("yoghurt", "0% Greek yoghurt", 70, "g", 42, 7.2, 2.8, 0.1, 0));
        foods.add(new Food("coffee_pod", "Nespresso coffee pod", 1, "pod", 0, 0, 0, 0, 0));
        foods.add(new Food("coffee_milk", "Skimmed milk - coffee", 100, "ml", 35, 3.5, 5.0, 0.1, 0));
        foods.add(new Food("tuna", "Tinned tuna in spring water", 90, "g drained", 103, 22.5, 0, 0.5, 0));
        foods.add(new Food("egg", "Large egg", 1, "egg", 78, 6.3, 0.6, 5.3, 0));
        foods.add(new Food("salad", "Mixed salad", 250, "g", 52, 2.7, 8.8, 0.7, 2.9));
        foods.add(new Food("mayo", "Hellmann's Real Mayonnaise", 18, "g", 123, 0.2, 0, 13.9, 0));
        foods.add(new Food("wrap", "Lidl Rowan Hill Wholemeal Wrap", 1, "wrap", 173, 5.7, 28.4, 3.0, 6.0));
        foods.add(new Food("chicken", "Chicken breast", 160, "g raw", 176, 37.4, 0, 2.0, 0));
        foods.add(new Food("potatoes", "New potatoes", 300, "g raw", 231, 6.0, 51.0, 0.3, 5.4));
        foods.add(new Food("veg", "Fresh vegetables", 300, "g", 99, 6.9, 21.0, 0.9, 9.9));
        foods.add(new Food("butter", "Unsalted butter", 5, "g", 36, 0, 0, 4.0, 0));
        foods.add(new Food("shake", "Bulk Complete Diet Protein Advanced", 60, "g / 2 scoops", 224, 44.0, 4.6, 2.9, 2.9));
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(14), dp(12), dp(14), dp(10));

        TextView title = text("Food Tracker", 26, TEXT, true);
        root.addView(title, lpMatchWrap());

        TextView subtitle = text("Strict diet • offline • actual food logged only", 13, MUTED, false);
        LinearLayout.LayoutParams subLp = lpMatchWrap();
        subLp.bottomMargin = dp(12);
        root.addView(subtitle, subLp);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER);

        todayTab = tabButton("TODAY");
        historyTab = tabButton("HISTORY");
        LinearLayout.LayoutParams tabLp = new LinearLayout.LayoutParams(0, dp(46), 1);
        tabLp.rightMargin = dp(5);
        tabs.addView(todayTab, tabLp);
        LinearLayout.LayoutParams tabLp2 = new LinearLayout.LayoutParams(0, dp(46), 1);
        tabLp2.leftMargin = dp(5);
        tabs.addView(historyTab, tabLp2);
        root.addView(tabs, lpMatchWrap());

        content = new FrameLayout(this);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        contentLp.topMargin = dp(10);
        root.addView(content, contentLp);

        todayTab.setOnClickListener(v -> showToday());
        historyTab.setOnClickListener(v -> showHistory());

        setContentView(root);
    }

    private void showToday() {
        setTabState(true);
        content.removeAllViews();
        loadTodayEntries();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0, 0, 0, dp(22));
        scroll.addView(body);

        LocalDate now = LocalDate.now();
        TextView date = text(now.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.UK)), 18, TEXT, true);
        LinearLayout.LayoutParams dateLp = lpMatchWrap();
        dateLp.bottomMargin = dp(10);
        body.addView(date, dateLp);

        LinearLayout totalsCard = cardLayout();
        TextView totalsTitle = text("TODAY'S TOTAL", 13, MUTED, true);
        totalsCard.addView(totalsTitle);

        kcalTotal = bigTotal();
        totalsCard.addView(kcalTotal);

        LinearLayout macroRow = new LinearLayout(this);
        macroRow.setOrientation(LinearLayout.HORIZONTAL);
        proteinTotal = miniTotal();
        carbsTotal = miniTotal();
        fatTotal = miniTotal();
        macroRow.addView(proteinTotal, new LinearLayout.LayoutParams(0, dp(62), 1));
        macroRow.addView(carbsTotal, new LinearLayout.LayoutParams(0, dp(62), 1));
        macroRow.addView(fatTotal, new LinearLayout.LayoutParams(0, dp(62), 1));
        totalsCard.addView(macroRow);

        fibreTotal = text("", 13, MUTED, false);
        LinearLayout.LayoutParams fibreLp = lpMatchWrap();
        fibreLp.topMargin = dp(6);
        totalsCard.addView(fibreTotal, fibreLp);
        body.addView(totalsCard, cardLp());

        LinearLayout chartCard = cardLayout();
        TextView chartTitle = text("MACRO SPLIT", 13, MUTED, true);
        chartCard.addView(chartTitle);
        pieView = new MacroPieView(this);
        LinearLayout.LayoutParams pieLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(190));
        pieLp.topMargin = dp(4);
        chartCard.addView(pieView, pieLp);
        macroLegend = text("", 14, TEXT, false);
        macroLegend.setGravity(Gravity.CENTER);
        chartCard.addView(macroLegend, lpMatchWrap());
        body.addView(chartCard, cardLp());

        LinearLayout logCard = cardLayout();
        logCard.addView(text("LOG FOOD", 13, MUTED, true));

        foodSpinner = new Spinner(this);
        List<String> foodNames = new ArrayList<>();
        for (Food f : foods) foodNames.add(f.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, foodNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView v = (TextView) super.getView(position, convertView, parent);
                v.setTextColor(TEXT);
                v.setTextSize(16);
                v.setPadding(dp(10), 0, dp(10), 0);
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView v = (TextView) super.getDropDownView(position, convertView, parent);
                v.setTextColor(Color.BLACK);
                v.setTextSize(16);
                v.setPadding(dp(14), dp(12), dp(14), dp(12));
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        foodSpinner.setAdapter(adapter);
        foodSpinner.setBackground(rounded(CARD_2, 12, 0, Color.TRANSPARENT));
        LinearLayout.LayoutParams spinLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        spinLp.topMargin = dp(8);
        logCard.addView(foodSpinner, spinLp);

        LinearLayout amountRow = new LinearLayout(this);
        amountRow.setOrientation(LinearLayout.HORIZONTAL);
        amountRow.setGravity(Gravity.CENTER_VERTICAL);
        amountInput = new EditText(this);
        amountInput.setTextColor(TEXT);
        amountInput.setTextSize(20);
        amountInput.setSingleLine(true);
        amountInput.setGravity(Gravity.CENTER);
        amountInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amountInput.setBackground(rounded(CARD_2, 12, 0, Color.TRANSPARENT));
        amountInput.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout.LayoutParams amountLp = new LinearLayout.LayoutParams(0, dp(54), 1);
        amountLp.topMargin = dp(10);
        amountRow.addView(amountInput, amountLp);

        unitLabel = text("", 16, MUTED, true);
        unitLabel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams unitLp = new LinearLayout.LayoutParams(dp(100), dp(54));
        unitLp.topMargin = dp(10);
        unitLp.leftMargin = dp(8);
        amountRow.addView(unitLabel, unitLp);
        logCard.addView(amountRow);

        logButton = new Button(this);
        logButton.setText("LOG FOOD");
        logButton.setTextColor(Color.WHITE);
        logButton.setTextSize(16);
        logButton.setAllCaps(false);
        logButton.setBackground(rounded(ACCENT, 12, 0, Color.TRANSPARENT));
        LinearLayout.LayoutParams logLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        logLp.topMargin = dp(10);
        logCard.addView(logButton, logLp);
        body.addView(logCard, cardLp());

        TextView eatenTitle = text("TODAY'S FOOD", 13, MUTED, true);
        LinearLayout.LayoutParams eatenTitleLp = lpMatchWrap();
        eatenTitleLp.topMargin = dp(4);
        eatenTitleLp.bottomMargin = dp(6);
        body.addView(eatenTitle, eatenTitleLp);
        loggedList = new LinearLayout(this);
        loggedList.setOrientation(LinearLayout.VERTICAL);
        body.addView(loggedList, lpMatchWrap());

        foodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (editingIndex < 0) fillDefaultAmount(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        logButton.setOnClickListener(v -> saveLogItem());

        fillDefaultAmount(0);
        refreshTodayUi();
        content.addView(scroll, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void fillDefaultAmount(int foodPosition) {
        Food f = foods.get(foodPosition);
        amountInput.setText(formatAmount(f.defaultAmount));
        unitLabel.setText(f.unit);
    }

    private void saveLogItem() {
        int position = foodSpinner.getSelectedItemPosition();
        if (position < 0) return;
        String raw = amountInput.getText().toString().trim();
        if (raw.isEmpty()) {
            Toast.makeText(this, "Enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(raw);
        } catch (Exception e) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }
        if (amount <= 0) {
            Toast.makeText(this, "Amount must be above zero", Toast.LENGTH_SHORT).show();
            return;
        }

        Food food = foods.get(position);
        Entry entry = Entry.fromFood(food, amount);
        if (editingIndex >= 0 && editingIndex < todayEntries.size()) {
            todayEntries.set(editingIndex, entry);
            editingIndex = -1;
            logButton.setText("LOG FOOD");
        } else {
            todayEntries.add(entry);
        }
        persistTodayEntries();
        fillDefaultAmount(position);
        refreshTodayUi();
    }

    private void refreshTodayUi() {
        Totals totals = totals(todayEntries);
        kcalTotal.setText(Math.round(totals.kcal) + " / " + Math.round(TARGET_KCAL) + " kcal");
        proteinTotal.setText("PROTEIN\n" + oneDp.format(totals.protein) + " / " + oneDp.format(TARGET_PROTEIN) + " g");
        carbsTotal.setText("CARBS\n" + oneDp.format(totals.carbs) + " / " + oneDp.format(TARGET_CARBS) + " g");
        fatTotal.setText("FAT\n" + oneDp.format(totals.fat) + " / " + oneDp.format(TARGET_FAT) + " g");
        fibreTotal.setText("Fibre  " + oneDp.format(totals.fibre) + " / " + oneDp.format(TARGET_FIBRE) + " g");

        double macroCalories = totals.protein * 4 + totals.carbs * 4 + totals.fat * 9;
        double pPct = macroCalories > 0 ? totals.protein * 4 * 100 / macroCalories : 0;
        double cPct = macroCalories > 0 ? totals.carbs * 4 * 100 / macroCalories : 0;
        double fPct = macroCalories > 0 ? totals.fat * 9 * 100 / macroCalories : 0;
        macroLegend.setText("Protein " + Math.round(pPct) + "%   •   Carbs " + Math.round(cPct) + "%   •   Fat " + Math.round(fPct) + "%");
        pieView.setMacros(totals.protein, totals.carbs, totals.fat);

        renderLoggedItems();
    }

    private void renderLoggedItems() {
        loggedList.removeAllViews();
        if (todayEntries.isEmpty()) {
            TextView empty = text("Nothing logged yet today.", 15, MUTED, false);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(12), dp(20), dp(12), dp(20));
            empty.setBackground(rounded(CARD, 14, 0, Color.TRANSPARENT));
            loggedList.addView(empty, lpMatchWrap());
            return;
        }

        for (int i = 0; i < todayEntries.size(); i++) {
            final int index = i;
            Entry e = todayEntries.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(9), dp(8), dp(9));
            row.setBackground(rounded(CARD, 13, 0, Color.TRANSPARENT));

            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            Food food = findFood(e.foodId);
            String unit = food != null ? food.unit : "";
            TextView n = text(e.foodName, 16, TEXT, true);
            TextView d = text(formatAmount(e.amount) + " " + unit + "  •  " + Math.round(e.kcal) + " kcal  •  P " + oneDp.format(e.protein) + " g", 13, MUTED, false);
            textCol.addView(n);
            textCol.addView(d);
            row.addView(textCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            Button delete = new Button(this);
            delete.setText("×");
            delete.setTextColor(Color.WHITE);
            delete.setTextSize(22);
            delete.setBackground(rounded(DANGER, 10, 0, Color.TRANSPARENT));
            LinearLayout.LayoutParams delLp = new LinearLayout.LayoutParams(dp(48), dp(46));
            delLp.leftMargin = dp(8);
            row.addView(delete, delLp);

            textCol.setOnClickListener(v -> editEntry(index));
            delete.setOnClickListener(v -> deleteEntry(index));

            LinearLayout.LayoutParams rowLp = lpMatchWrap();
            rowLp.bottomMargin = dp(7);
            loggedList.addView(row, rowLp);
        }
    }

    private void editEntry(int index) {
        Entry e = todayEntries.get(index);
        int pos = findFoodPosition(e.foodId);
        if (pos < 0) return;
        editingIndex = index;
        foodSpinner.setSelection(pos);
        amountInput.setText(formatAmount(e.amount));
        unitLabel.setText(foods.get(pos).unit);
        logButton.setText("UPDATE ITEM");
        amountInput.requestFocus();
    }

    private void deleteEntry(int index) {
        Entry e = todayEntries.get(index);
        new AlertDialog.Builder(this)
                .setTitle("Delete item?")
                .setMessage(e.foodName + " • " + formatAmount(e.amount))
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    todayEntries.remove(index);
                    editingIndex = -1;
                    logButton.setText("LOG FOOD");
                    persistTodayEntries();
                    refreshTodayUi();
                })
                .show();
    }

    private void showHistory() {
        setTabState(false);
        content.removeAllViews();
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0, 0, 0, dp(22));
        scroll.addView(body);

        TextView title = text("Daily history", 20, TEXT, true);
        LinearLayout.LayoutParams titleLp = lpMatchWrap();
        titleLp.bottomMargin = dp(10);
        body.addView(title, titleLp);

        HistorySummary summary = calculateSevenDaySummary();
        LinearLayout summaryCard = cardLayout();
        summaryCard.addView(text("LAST 7 DAYS", 13, MUTED, true));
        if (summary.daysLogged == 0) {
            TextView noData = text("No logged days yet.", 15, TEXT, false);
            LinearLayout.LayoutParams noLp = lpMatchWrap();
            noLp.topMargin = dp(8);
            summaryCard.addView(noData, noLp);
        } else {
            summaryCard.addView(text(Math.round(summary.avgKcal) + " kcal average", 25, TEXT, true), lpTop(6));
            summaryCard.addView(text(oneDp.format(summary.avgProtein) + " g protein average", 16, TEXT, false), lpTop(4));
            summaryCard.addView(text("Carbs " + oneDp.format(summary.avgCarbs) + " g   •   Fat " + oneDp.format(summary.avgFat) + " g average", 14, TEXT, false), lpTop(4));
            summaryCard.addView(text("Protein target hit " + summary.proteinTargetDays + " / " + summary.daysLogged + " logged days", 14, MUTED, false), lpTop(4));
        }
        body.addView(summaryCard, cardLp());

        List<HistoryDay> days = loadHistoryDays();
        if (days.isEmpty()) {
            TextView empty = text("Your completed days will appear here automatically as you log food.", 15, MUTED, false);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(20), dp(28), dp(20), dp(28));
            empty.setBackground(rounded(CARD, 14, 0, Color.TRANSPARENT));
            body.addView(empty, lpMatchWrap());
        } else {
            for (HistoryDay day : days) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(dp(14), dp(12), dp(14), dp(12));
                row.setBackground(rounded(CARD, 14, 0, Color.TRANSPARENT));

                String dateText;
                try {
                    LocalDate d = LocalDate.parse(day.dateKey);
                    dateText = d.format(DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.UK));
                } catch (Exception ex) {
                    dateText = day.dateKey;
                }
                row.addView(text(dateText, 16, TEXT, true));
                row.addView(text(Math.round(day.totals.kcal) + " kcal   •   P " + oneDp.format(day.totals.protein) + " g   •   C " + oneDp.format(day.totals.carbs) + " g   •   F " + oneDp.format(day.totals.fat) + " g", 13, MUTED, false), lpTop(4));
                row.setOnClickListener(v -> showDayDetail(day));

                LinearLayout.LayoutParams rowLp = lpMatchWrap();
                rowLp.bottomMargin = dp(8);
                body.addView(row, rowLp);
            }
        }

        content.addView(scroll, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void showDayDetail(HistoryDay day) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(10), dp(18), dp(10));
        scroll.addView(box);

        box.addView(text(Math.round(day.totals.kcal) + " kcal", 22, Color.BLACK, true));
        box.addView(dialogText("Protein " + oneDp.format(day.totals.protein) + " g   •   Carbs " + oneDp.format(day.totals.carbs) + " g   •   Fat " + oneDp.format(day.totals.fat) + " g"));
        box.addView(dialogText("Fibre " + oneDp.format(day.totals.fibre) + " g"));

        for (Entry e : day.entries) {
            Food f = findFood(e.foodId);
            String unit = f != null ? f.unit : "";
            TextView item = dialogText("• " + e.foodName + " — " + formatAmount(e.amount) + " " + unit + " (" + Math.round(e.kcal) + " kcal)");
            LinearLayout.LayoutParams ilp = lpMatchWrap();
            ilp.topMargin = dp(7);
            box.addView(item, ilp);
        }

        new AlertDialog.Builder(this)
                .setTitle("Food log")
                .setView(scroll)
                .setPositiveButton("Close", null)
                .show();
    }

    private TextView dialogText(String value) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(Color.rgb(45, 45, 45));
        t.setTextSize(14);
        return t;
    }

    private void setTabState(boolean today) {
        todayTab.setBackground(rounded(today ? ACCENT : CARD_2, 12, 0, Color.TRANSPARENT));
        historyTab.setBackground(rounded(today ? CARD_2 : ACCENT, 12, 0, Color.TRANSPARENT));
    }

    private Button tabButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setBackground(rounded(CARD_2, 12, 0, Color.TRANSPARENT));
        return b;
    }

    private TextView bigTotal() {
        TextView t = text("", 30, TEXT, true);
        t.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(60));
        t.setLayoutParams(lp);
        return t;
    }

    private TextView miniTotal() {
        TextView t = text("", 14, TEXT, true);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private LinearLayout cardLayout() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(rounded(CARD, 16, 0, Color.TRANSPARENT));
        return card;
    }

    private LinearLayout.LayoutParams cardLp() {
        LinearLayout.LayoutParams lp = lpMatchWrap();
        lp.bottomMargin = dp(10);
        return lp;
    }

    private LinearLayout.LayoutParams lpTop(int topDp) {
        LinearLayout.LayoutParams lp = lpMatchWrap();
        lp.topMargin = dp(topDp);
        return lp;
    }

    private LinearLayout.LayoutParams lpMatchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private TextView text(String value, float sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        return t;
    }

    private GradientDrawable rounded(int fill, float radiusDp, float strokeDp, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) d.setStroke(dp(strokeDp), strokeColor);
        return d;
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String todayKey() {
        return "entries_" + LocalDate.now();
    }

    private void loadTodayEntries() {
        todayEntries.clear();
        todayEntries.addAll(readEntries(todayKey()));
        editingIndex = -1;
    }

    private List<Entry> readEntries(String key) {
        List<Entry> out = new ArrayList<>();
        String raw = prefs.getString(key, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                Entry e = Entry.fromJson(o, this);
                if (e != null) out.add(e);
            }
        } catch (Exception ignored) { }
        return out;
    }

    private void persistTodayEntries() {
        JSONArray array = new JSONArray();
        try {
            for (Entry e : todayEntries) array.put(e.toJson());
        } catch (Exception ignored) { }
        prefs.edit().putString(todayKey(), array.toString()).apply();
    }

    private List<HistoryDay> loadHistoryDays() {
        List<HistoryDay> days = new ArrayList<>();
        Map<String, ?> all = prefs.getAll();
        for (String key : all.keySet()) {
            if (!key.startsWith("entries_")) continue;
            List<Entry> entries = readEntries(key);
            if (entries.isEmpty()) continue;
            days.add(new HistoryDay(key.substring("entries_".length()), entries, totals(entries)));
        }
        Collections.sort(days, Comparator.comparing((HistoryDay d) -> d.dateKey).reversed());
        return days;
    }

    private HistorySummary calculateSevenDaySummary() {
        double kcal = 0;
        double protein = 0;
        double carbs = 0;
        double fat = 0;
        int logged = 0;
        int proteinHits = 0;
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            String key = "entries_" + today.minusDays(i);
            List<Entry> entries = readEntries(key);
            if (entries.isEmpty()) continue;
            Totals t = totals(entries);
            kcal += t.kcal;
            protein += t.protein;
            carbs += t.carbs;
            fat += t.fat;
            logged++;
            if (t.protein >= TARGET_PROTEIN) proteinHits++;
        }
        HistorySummary s = new HistorySummary();
        s.daysLogged = logged;
        s.proteinTargetDays = proteinHits;
        if (logged > 0) {
            s.avgKcal = kcal / logged;
            s.avgProtein = protein / logged;
            s.avgCarbs = carbs / logged;
            s.avgFat = fat / logged;
        }
        return s;
    }

    private Totals totals(List<Entry> entries) {
        Totals t = new Totals();
        for (Entry e : entries) {
            t.kcal += e.kcal;
            t.protein += e.protein;
            t.carbs += e.carbs;
            t.fat += e.fat;
            t.fibre += e.fibre;
        }
        return t;
    }

    private Food findFood(String id) {
        for (Food f : foods) if (f.id.equals(id)) return f;
        return null;
    }

    private int findFoodPosition(String id) {
        for (int i = 0; i < foods.size(); i++) if (foods.get(i).id.equals(id)) return i;
        return -1;
    }

    private String formatAmount(double amount) {
        if (Math.abs(amount - Math.rint(amount)) < 0.0001) return String.valueOf((long) Math.rint(amount));
        return oneDp.format(amount);
    }

    private static class Food {
        final String id;
        final String name;
        final double defaultAmount;
        final String unit;
        final double kcal;
        final double protein;
        final double carbs;
        final double fat;
        final double fibre;

        Food(String id, String name, double defaultAmount, String unit, double kcal, double protein, double carbs, double fat, double fibre) {
            this.id = id;
            this.name = name;
            this.defaultAmount = defaultAmount;
            this.unit = unit;
            this.kcal = kcal;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
            this.fibre = fibre;
        }
    }

    private static class Entry {
        String foodId;
        String foodName;
        double amount;
        double kcal;
        double protein;
        double carbs;
        double fat;
        double fibre;

        static Entry fromFood(Food food, double amount) {
            double ratio = amount / food.defaultAmount;
            Entry e = new Entry();
            e.foodId = food.id;
            e.foodName = food.name;
            e.amount = amount;
            e.kcal = food.kcal * ratio;
            e.protein = food.protein * ratio;
            e.carbs = food.carbs * ratio;
            e.fat = food.fat * ratio;
            e.fibre = food.fibre * ratio;
            return e;
        }

        JSONObject toJson() throws Exception {
            JSONObject o = new JSONObject();
            o.put("foodId", foodId);
            o.put("foodName", foodName);
            o.put("amount", amount);
            o.put("kcal", kcal);
            o.put("protein", protein);
            o.put("carbs", carbs);
            o.put("fat", fat);
            o.put("fibre", fibre);
            return o;
        }

        static Entry fromJson(JSONObject o, MainActivity activity) {
            try {
                Entry e = new Entry();
                e.foodId = o.getString("foodId");
                e.foodName = o.optString("foodName", e.foodId);
                e.amount = o.getDouble("amount");
                if (o.has("kcal")) {
                    e.kcal = o.getDouble("kcal");
                    e.protein = o.getDouble("protein");
                    e.carbs = o.getDouble("carbs");
                    e.fat = o.getDouble("fat");
                    e.fibre = o.optDouble("fibre", 0);
                } else {
                    Food f = activity.findFood(e.foodId);
                    if (f == null) return null;
                    return fromFood(f, e.amount);
                }
                return e;
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private static class Totals {
        double kcal;
        double protein;
        double carbs;
        double fat;
        double fibre;
    }

    private static class HistoryDay {
        final String dateKey;
        final List<Entry> entries;
        final Totals totals;
        HistoryDay(String dateKey, List<Entry> entries, Totals totals) {
            this.dateKey = dateKey;
            this.entries = entries;
            this.totals = totals;
        }
    }

    private static class HistorySummary {
        int daysLogged;
        int proteinTargetDays;
        double avgKcal;
        double avgProtein;
        double avgCarbs;
        double avgFat;
    }

    private class MacroPieView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private double p;
        private double c;
        private double f;

        MacroPieView(android.content.Context context) {
            super(context);
        }

        void setMacros(double protein, double carbs, double fat) {
            p = protein;
            c = carbs;
            f = fat;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float size = Math.min(getWidth(), getHeight()) - dp(28);
            float left = (getWidth() - size) / 2f;
            float top = (getHeight() - size) / 2f;
            RectF oval = new RectF(left, top, left + size, top + size);
            double pc = p * 4;
            double cc = c * 4;
            double fc = f * 9;
            double total = pc + cc + fc;

            if (total <= 0.0001) {
                paint.setColor(CARD_2);
                canvas.drawOval(oval, paint);
            } else {
                float start = -90f;
                float pSweep = (float) (pc / total * 360f);
                float cSweep = (float) (cc / total * 360f);
                float fSweep = 360f - pSweep - cSweep;
                paint.setColor(PROTEIN);
                canvas.drawArc(oval, start, pSweep, true, paint);
                start += pSweep;
                paint.setColor(CARBS);
                canvas.drawArc(oval, start, cSweep, true, paint);
                start += cSweep;
                paint.setColor(FAT);
                canvas.drawArc(oval, start, fSweep, true, paint);
            }

            paint.setColor(CARD);
            float inner = size * 0.55f;
            RectF hole = new RectF((getWidth() - inner) / 2f, (getHeight() - inner) / 2f,
                    (getWidth() + inner) / 2f, (getHeight() + inner) / 2f);
            canvas.drawOval(hole, paint);

            paint.setColor(TEXT);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            paint.setTextSize(dp(15));
            canvas.drawText(total <= 0.0001 ? "No food" : "Macros", getWidth() / 2f, getHeight() / 2f + dp(5), paint);
        }
    }
}
