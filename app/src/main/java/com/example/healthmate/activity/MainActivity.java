package com.example.healthmate.activity;

import com.example.healthmate.R;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import java.text.SimpleDateFormat;
import java.util.*;
import com.example.healthmate.model.HealthRecord;
import com.example.healthmate.database.HealthDatabaseHelper;
import com.example.healthmate.adapter.RecordTextAdapter;

public class MainActivity extends AppCompatActivity {
    int BLUE = Color.rgb(57,120,246), NAVY = Color.rgb(20,43,74), MUTED = Color.rgb(130,144,165);
    int GREEN = Color.rgb(25,184,137), ORANGE = Color.rgb(255,152,96);
    int background, surface, mainText, mutedText;
    boolean darkMode, loggedIn = false;
    String userName = "";
    int currentPage = 0;
    int editId = -1;
    LinearLayout root, content, header;
    HealthDatabaseHelper database;
    ArrayList<HealthRecord> records = new ArrayList<>();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        database = new HealthDatabaseHelper(this);
        database.addSampleData(today());
        darkMode = getPreferences(MODE_PRIVATE).getBoolean("dark", false);

        // Keep the user signed in when the app is closed and opened again.
        userName = getPreferences(MODE_PRIVATE).getString("saved_username", "");
        loggedIn = getPreferences(MODE_PRIVATE).getBoolean("logged_in", false);

        setColors();

        if (loggedIn && !userName.isEmpty()) {
            showMain();
        } else {
            showLogin();
        }
    }

    void setColors() {
        if (darkMode) {
            background = Color.rgb(16,24,39);
            surface = Color.rgb(28,41,59);
            mainText = Color.rgb(234,240,255);
            mutedText = Color.rgb(160,174,192);
        } else {
            background = Color.rgb(234,240,248);
            surface = Color.WHITE;
            mainText = NAVY;
            mutedText = MUTED;
        }
        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(surface);

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();

            if (darkMode) {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            } else {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }

            getWindow().getDecorView().setSystemUiVisibility(flags);
        } else if (android.os.Build.VERSION.SDK_INT >= 23) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();

            if (darkMode) {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }

            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            getWindow().setNavigationBarContrastEnforced(false);
        }
    }

    int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density); }

    GradientDrawable shape(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    GradientDrawable gradient(int first, int second, int radius) {
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{first, second});
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    LinearLayout row() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        return layout;
    }

    void addSpace(LinearLayout parent, int height) {
        View space = new View(this);
        parent.addView(space, new LinearLayout.LayoutParams(1, dp(height)));
    }

    void showLogin() {
        setColors();
        root = column();
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(18), dp(24), dp(18));
        root.setBackground(gradient(Color.rgb(18,43,80), Color.rgb(57,120,246), 0));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.healthmate_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(88), dp(88));
        logoParams.gravity = Gravity.CENTER;
        root.addView(logo, logoParams);
        addSpace(root, 10);

        TextView title = text("HealthMate", 30, Color.WHITE, true);
        title.setGravity(Gravity.CENTER);
        root.addView(title);
        TextView subtitle = text("Your wellness journey starts here", 14, Color.WHITE, false);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);
        addSpace(root, 22);

        LinearLayout card = column();
        card.setPadding(dp(22), dp(22), dp(22), dp(22));
        card.setBackground(shape(Color.WHITE, 25));
        root.addView(card, new LinearLayout.LayoutParams(-1, -2));
        card.addView(text("Welcome back", 23, NAVY, true));
        card.addView(text("Sign in with your username and password.", 13, MUTED, false));
        addSpace(card, 16);

        EditText username = field("Username");
        username.setInputType( android.text.InputType.TYPE_CLASS_TEXT);
        card.addView(username);
        addSpace(card, 10);
        EditText password = field("Password");
        password.setInputType(129);
        card.addView(password);
        addSpace(card, 14);

        TextView error = text("", 12, Color.RED, false);
        card.addView(error);
        Button login = button("Sign in", BLUE);
        card.addView(login, new LinearLayout.LayoutParams(-1, dp(52)));

        TextView create = text("Create a new account", 14, BLUE, true);
        create.setGravity(Gravity.CENTER);
        create.setPadding(0, dp(16), 0, dp(4));
        card.addView(create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateAccount();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString().trim();
                String pass = password.getText().toString();
                if (user.isEmpty() || pass.isEmpty()) {
                    error.setText("Enter your username and password.");
                } else if (database.checkLogin(user, pass)) {
                    userName = user;
                    loggedIn = true;

                    // Save login details for the next app opening.
                    getPreferences(MODE_PRIVATE).edit()
                        .putBoolean("logged_in", true)
                        .putString("saved_username", userName)
                        .apply();

                    currentPage = 0;
                    showMain();
                } else {
                    error.setText("Incorrect username or password.");
                }
            }
        });
        setContentView(root);
        applyTopInset(root);
    }

    void showCreateAccount() {
        setColors();
        root = column();
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(18), dp(24), dp(18));
        root.setBackground(gradient(Color.rgb(18,43,80), Color.rgb(57,120,246), 0));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.healthmate_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(76), dp(76));
        logoParams.gravity = Gravity.CENTER;
        root.addView(logo, logoParams);
        addSpace(root, 10);

        TextView title = text("Create Account", 27, Color.WHITE, true);
        title.setGravity(Gravity.CENTER);
        root.addView(title);
        TextView subtitle = text("Create your personal HealthMate account", 13, Color.WHITE, false);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);
        addSpace(root, 22);

        LinearLayout card = column();
        card.setPadding(dp(22), dp(22), dp(22), dp(22));
        card.setBackground(shape(Color.WHITE, 25));
        root.addView(card, new LinearLayout.LayoutParams(-1, -2));
        card.addView(text("Your details", 22, NAVY, true));
        addSpace(card, 14);

        EditText username = field("Choose a username");
        card.addView(username);
        addSpace(card, 10);
        EditText password = field("Create password");
        password.setInputType(129);
        card.addView(password);
        addSpace(card, 10);
        EditText confirm = field("Confirm password");
        confirm.setInputType(129);
        card.addView(confirm);
        addSpace(card, 12);

        TextView error = text("", 12, Color.RED, false);
        card.addView(error);
        Button create = button("Create account", BLUE);
        card.addView(create, new LinearLayout.LayoutParams(-1, dp(52)));

        TextView back = text("Already have an account? Sign in", 13, BLUE, true);
        back.setGravity(Gravity.CENTER);
        back.setPadding(0, dp(16), 0, dp(4));
        card.addView(back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogin();
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString().trim();
                String pass = password.getText().toString();
                String confirmPass = confirm.getText().toString();

                if (user.length() < 3) {
                    error.setText("Username must have at least 3 characters.");
                } else if (pass.length() < 4) {
                    error.setText("Password must have at least 4 characters.");
                } else if (!pass.equals(confirmPass)) {
                    error.setText("Passwords do not match.");
                } else if (database.userExists(user)) {
                    error.setText("This username is already taken.");
                } else {
                    if (database.createUser(user, pass)) {
                        Toast.makeText(MainActivity.this, "Account created. Please sign in.", Toast.LENGTH_LONG).show();
                        showLogin();
                    } else {
                        error.setText("Could not create the account. Please try again.");
                    }
                }
            }
        });

        setContentView(root);
        applyTopInset(root);
    }

    void applyTopInset(View view) {
        view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int top = insets.getSystemWindowInsetTop();
                v.setPadding(v.getPaddingLeft(), dp(12) + top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            }
        });
        view.requestApplyInsets();
    }

    EditText field(String hint) {
        EditText edit = new EditText(this);
        edit.setSingleLine(true);
        edit.setTextSize(14);
        edit.setHint(hint);
        edit.setPadding(dp(14), dp(10), dp(14), dp(10));
        int fieldColor = Color.rgb(247,249,252);
        int fieldText = NAVY;
        if (loggedIn && darkMode) {
            fieldColor = surface;
            fieldText = mainText;
        }
        edit.setBackground(shape(fieldColor, 14));
        edit.setTextColor(fieldText);
        edit.setHintTextColor(mutedText);
        return edit;
    }

    Button button(String label, int color) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        button.setBackground(shape(color, 15));
        return button;
    }

    void showMain() {
        setColors();
        root = column();
        root.setBackgroundColor(background);
        header = row();
        header.setPadding(dp(16), dp(12), dp(16), dp(12));
        header.setBackgroundColor(surface);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.healthmate_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        header.addView(logo, new LinearLayout.LayoutParams(dp(42), dp(42)));
        LinearLayout brand = column();
        brand.setPadding(dp(10), 0, 0, 0);
        brand.addView(text("HealthMate", 16, mainText, true));
        brand.addView(text("PERSONAL WELLNESS", 8, mutedText, false));
        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        TextView initial = text(userName.isEmpty() ? "U" : userName.substring(0,1).toUpperCase(), 16, Color.WHITE, true);
        initial.setGravity(Gravity.CENTER);
        initial.setBackground(shape(BLUE, 30));
        header.addView(initial, new LinearLayout.LayoutParams(dp(38), dp(38)));
        root.addView(header);

        content = column();
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        showPage();

        LinearLayout nav = row();
        nav.setPadding(dp(8), dp(6), dp(8), dp(6));
        nav.setBackgroundColor(surface);

        int[] navIcons = {
            R.drawable.ic_nav_home,
            R.drawable.ic_nav_records,
            R.drawable.ic_nav_add,
            R.drawable.ic_nav_settings
        };
        String[] navLabels = {"Home", "Records", "Add", "Settings"};

        for (int i = 0; i < navLabels.length; i++) {
            final int page = i;
            final boolean selected = currentPage == i;

            LinearLayout item = column();
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(4), dp(5), dp(4), dp(5));

            if (selected) {
                item.setBackground(shape(darkMode ? Color.rgb(49, 64, 91) : Color.rgb(235, 228, 250), 18));
            }

            ImageView icon = new ImageView(this);
            icon.setImageResource(navIcons[i]);
            icon.setColorFilter(selected ? BLUE : mainText);
            item.addView(icon, new LinearLayout.LayoutParams(dp(23), dp(23)));

            TextView label = text(navLabels[i], 11, selected ? BLUE : mainText, selected);
            label.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-1, -2);
            labelParams.topMargin = dp(3);
            item.addView(label, labelParams);

            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(0, dp(54), 1);
            itemParams.setMargins(dp(3), 0, dp(3), 0);
            nav.addView(item, itemParams);

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPage = page;
                    editId = -1;
                    showMain();
                }
            });
        }
        root.addView(nav);
        setContentView(root);
        applyMainTopInset(root);
    }

    void applyMainTopInset(View view) {
        view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int top = insets.getSystemWindowInsetTop();
                int bottom = insets.getSystemWindowInsetBottom();

                header.setPadding(dp(16), dp(12) + top, dp(16), dp(12));

                // Keep the app navigation bar above Android system navigation buttons.
                v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    bottom
                );
                return insets;
            }
        });
        view.requestApplyInsets();
    }

    void showMainNavOnly() {
        // Rebuild the page so the selected navigation item is updated.
        showMain();
    }

    void showPage() {
        content.removeAllViews();
        content.setPadding(dp(18), dp(18), dp(18), dp(18));
        if (currentPage == 0) showDashboard();
        else if (currentPage == 1) showRecords();
        else if (currentPage == 2) showAddRecord();
        else showSettings();
    }

    ArrayList<HealthRecord> getRecords() {
        records.clear();
        records.addAll(database.getAllRecords());
        return records;
    }

    void showDashboard() {
        ArrayList<HealthRecord> list = getRecords();
        String today = today();
        int[] totals = database.getTodaySummary(today);
        int steps = totals[0];
        int calories = totals[1];
        int water = totals[2];
        content.addView(text("Good day, let's move! ✨", 13, BLUE, true));
        content.addView(text("Your health overview", 25, mainText, true));
        content.addView(text("▦  " + displayDate(today), 12, mutedText, false));
        addSpace(content, 16);

        LinearLayout hero = column();
        hero.setPadding(dp(18), dp(20), dp(18), dp(20));
        hero.setBackground(gradient(Color.rgb(36,86,181), Color.rgb(90,185,236), 23));
        hero.addView(text("TODAY'S ACTIVITY", 10, Color.WHITE, true));
        addSpace(hero, 8);
        hero.addView(text(steps + " steps", 34, Color.WHITE, true));
        hero.addView(text("Keep going. Every step counts!", 12, Color.WHITE, false));
        addSpace(hero, 16);
        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(10000);
        progress.setProgress(Math.min(steps, 10000));
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0x66FFFFFF));
        hero.addView(progress, new LinearLayout.LayoutParams(-1, dp(8)));
        hero.addView(text((Math.min(steps, 10000) * 100 / 10000) + "% of 10,000 step goal", 10, Color.WHITE, false));
        content.addView(hero);
        addSpace(content, 14);

        LinearLayout metrics = row();
        metrics.addView(metricCard("Calories", String.valueOf(calories), "kcal burned", "♨", ORANGE), new LinearLayout.LayoutParams(0, dp(136), 1));
        View gap = new View(this); metrics.addView(gap, new LinearLayout.LayoutParams(dp(10), 1));
        metrics.addView(metricCard("Water", String.valueOf(water), "ml consumed", "💧", BLUE), new LinearLayout.LayoutParams(0, dp(136), 1));
        content.addView(metrics);
        addSpace(content, 14);

        LinearLayout chartCard = card();
        chartCard.setPadding(dp(16), dp(16), dp(16), dp(16));
        chartCard.addView(text("Weekly steps", 17, mainText, true));
        chartCard.addView(text("Your last 7 days", 11, mutedText, false));
        addSpace(chartCard, 12);
        chartCard.addView(new WeeklyChart(this, list), new LinearLayout.LayoutParams(-1, dp(130)));
        content.addView(chartCard);
        addSpace(content, 14);
        content.addView(text("Your daily records", 17, mainText, true));
        content.addView(text(list.size() + " entries saved", 11, mutedText, false));
        int count = list.size();
        if (count > 2) count = 2;
        for (int i = 0; i < count; i++) {
            HealthRecord r = list.get(i);
            addSpace(content, 8);
            LinearLayout item = card(); item.setPadding(dp(14), dp(12), dp(14), dp(12));
            item.addView(text(r.date, 13, mainText, true));
            item.addView(text(r.steps + " steps  •  " + r.calories + " kcal  •  " + r.water + " ml", 11, mutedText, false));
            content.addView(item);
        }
    }

    LinearLayout metricCard(String title, String value, String subtitle, String icon, int tint) {
        LinearLayout box = card(); box.setPadding(dp(14), dp(12), dp(10), dp(12));
        TextView symbol = text(icon, 18, tint, true); symbol.setGravity(Gravity.CENTER); symbol.setBackground(shape(darkMode ? 0xFF263B55 : 0xFFFFF0E9, 12));
        box.addView(symbol, new LinearLayout.LayoutParams(dp(34), dp(34)));
        addSpace(box, 10);
        box.addView(text(title, 11, mutedText, false));
        box.addView(text(value, 22, mainText, true));
        box.addView(text(subtitle, 10, mutedText, false));
        return box;
    }

    LinearLayout card() {
        LinearLayout box = column();
        box.setBackground(shape(surface, 20));
        return box;
    }

    void showRecords() {
        content.addView(text("Health records", 25, mainText, true));
        content.addView(text("Review and manage your daily activity", 12, mutedText, false));
        addSpace(content, 12);
        EditText search = field("Search by date (YYYY-MM-DD)");
        content.addView(search);
        TextView clearSearch = text("Clear search", 12, BLUE, true);
        clearSearch.setGravity(Gravity.RIGHT);
        clearSearch.setPadding(0, dp(8), 0, dp(4));
        content.addView(clearSearch);
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { search.setText(""); }
        });
        LinearLayout results = column();
        content.addView(results);
        fillRecords(results, "");
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { fillRecords(results, s.toString()); }
            public void afterTextChanged(android.text.Editable e) {}
        });
    }

    void fillRecords(LinearLayout results, String search) {
        results.removeAllViews();
        ArrayList<HealthRecord> list = search.trim().isEmpty()
                ? database.getAllRecords() : database.searchRecordsByDate(search);
        int found = 0;
        for (int i = 0; i < list.size(); i++) {
            HealthRecord r = list.get(i);
            {
                found++;
                addSpace(results, 10);
                LinearLayout item = card(); item.setPadding(dp(14), dp(14), dp(14), dp(12));
                LinearLayout top = row();
                top.addView(text(r.date, 14, mainText, true), new LinearLayout.LayoutParams(0, -2, 1));
                top.addView(text("DAILY LOG", 9, BLUE, true));
                item.addView(top);
                addSpace(item, 12);
                item.addView(RecordTextAdapter.createSummary(this, r, mutedText));
                LinearLayout actions = row(); actions.setGravity(Gravity.RIGHT);
                TextView edit = text("Edit", 13, BLUE, true); edit.setPadding(dp(12), dp(10), dp(12), dp(8));
                TextView delete = text("Delete", 13, Color.rgb(216,75,85), true); delete.setPadding(dp(12), dp(10), dp(8), dp(8));
                actions.addView(edit); actions.addView(delete); item.addView(actions);
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editId = r.id;
                        currentPage = 2;
                        showMain();
                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete record?")
                            .setMessage("Do you want to delete this health record?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    database.deleteRecord(r.id);
                                    Toast.makeText(MainActivity.this, "Record deleted.", Toast.LENGTH_SHORT).show();
                                    fillRecords(results, search);
                                }
                            }).show();
                    }
                });
                results.addView(item);
            }
        }
        if (found == 0) results.addView(text("No records found. Add a new record.", 13, mutedText, false));
    }

    void showAddRecord() {
        HealthRecord old = null;
        if (editId != -1) old = database.findRecord(editId);
        content.addView(text(old == null ? "Add health record" : "Edit health record", 25, mainText, true));
        content.addView(text("Small daily actions make a big difference.", 12, mutedText, false));
        addSpace(content, 14);
        LinearLayout form = card(); form.setPadding(dp(16), dp(16), dp(16), dp(16));
        form.addView(text("ACTIVITY DETAILS", 10, BLUE, true)); addSpace(form, 12);
        EditText date = field("Date (YYYY-MM-DD)"); date.setText(old == null ? today() : old.date); form.addView(date);
        addSpace(form, 10);
        EditText steps = numberField("Steps walked", old == null ? "" : String.valueOf(old.steps)); form.addView(steps);
        addSpace(form, 10);
        EditText calories = numberField("Calories burned (kcal)", old == null ? "" : String.valueOf(old.calories)); form.addView(calories);
        addSpace(form, 10);
        EditText water = numberField("Water intake (ml)", old == null ? "" : String.valueOf(old.water)); form.addView(water);
        addSpace(form, 16);
        TextView error = text("", 12, Color.RED, false); form.addView(error);
        Button save = button(old == null ? "Save health record" : "Update health record", BLUE);
        form.addView(save, new LinearLayout.LayoutParams(-1, dp(52)));
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String d = date.getText().toString().trim();
                int s = toNumber(steps.getText().toString());
                int c = toNumber(calories.getText().toString());
                int w = toNumber(water.getText().toString());
                if (!validDate(d)) error.setText("Enter a valid date using YYYY-MM-DD.");
                else if (steps.getText().toString().isEmpty() || calories.getText().toString().isEmpty() || water.getText().toString().isEmpty()) error.setText("Please enter numbers in all fields.");
                else if (s < 0 || c < 0 || w < 0) error.setText("Values cannot be negative.");
                else {
                    long savedId;
                    if (editId == -1) savedId = database.insertRecord(d, s, c, w);
                    else savedId = database.updateRecord(editId, d, s, c, w);
                    if (savedId <= 0) {
                        error.setText("Could not save the record. Please try again.");
                        return;
                    }
                    Toast.makeText(MainActivity.this, "Health record saved.", Toast.LENGTH_SHORT).show();
                    editId = -1;
                    currentPage = 1;
                    showMain();
                }
            }
        });
        content.addView(form);
        addSpace(content, 12);
        content.addView(text("Your information is stored on this device.", 11, mutedText, false));
    }

    EditText numberField(String hint, String value) {
        EditText edit = field(hint); edit.setInputType(2); edit.setText(value); return edit;
    }

    int toNumber(String value) {
        try { return Integer.parseInt(value); } catch (Exception e) { return -1; }
    }

    boolean validDate(String value) {
        try {
            if (value == null || !value.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            java.util.Date parsed = format.parse(value);
            return parsed != null && format.format(parsed).equals(value);
        } catch (Exception e) { return false; }
    }

    void showSettings() {
        content.addView(text("Settings", 25, mainText, true));
        content.addView(text("Manage your account and appearance", 12, mutedText, false));
        addSpace(content, 18);
        LinearLayout profile = card(); profile.setPadding(dp(16), dp(16), dp(16), dp(16));
        profile.addView(text("PROFILE", 10, BLUE, true)); addSpace(profile, 8);
        profile.addView(text(userName, 18, mainText, true));
        profile.addView(text("HealthMate member", 12, mutedText, false));
        content.addView(profile);
        addSpace(content, 14);
        LinearLayout appearance = card(); appearance.setPadding(dp(16), dp(16), dp(16), dp(16));
        appearance.addView(text("APPEARANCE", 10, BLUE, true)); addSpace(appearance, 8);
        LinearLayout themeRow = row();
        LinearLayout labels = column();
        labels.addView(text("Dark theme", 15, mainText, true));
        labels.addView(text("Switch between light and dark mode", 11, mutedText, false));
        themeRow.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        SwitchCompat themeSwitch = new SwitchCompat(this); themeSwitch.setChecked(darkMode); themeRow.addView(themeSwitch);
        appearance.addView(themeRow);
        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                darkMode = checked;
                getPreferences(MODE_PRIVATE).edit().putBoolean("dark", darkMode).apply();
                setColors();
                showMain();
            }
        });
        content.addView(appearance);
        addSpace(content, 14);
        Button logout = button("Log out", Color.rgb(216,75,85));
        content.addView(logout, new LinearLayout.LayoutParams(-1, dp(52)));
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Log out")
                    .setMessage("Are you sure you want to log out?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Log out", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loggedIn = false;
                            userName = "";

                            // Remove saved login only after the user logs out.
                            getPreferences(MODE_PRIVATE).edit()
                                .putBoolean("logged_in", false)
                                .remove("saved_username")
                                .apply();

                            currentPage = 0;
                            showLogin();
                        }
                    }).show();
            }
        });
    }

    String today() { return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()); }
    String displayDate(String date) { return date; }

    class WeeklyChart extends View {
        Paint paint = new Paint(3);
        ArrayList<HealthRecord> data;
        WeeklyChart(Context context, ArrayList<HealthRecord> records) { super(context); data = records; }
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float width = getWidth(); float height = getHeight();
            int[] totals = new int[7];
            Calendar cal = Calendar.getInstance();
            for (int day = 6; day >= 0; day--) {
                Calendar target = (Calendar) cal.clone(); target.add(Calendar.DAY_OF_YEAR, -day);
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(target.getTime());
                for (int i = 0; i < data.size(); i++) if (data.get(i).date.equals(date)) totals[6-day] += data.get(i).steps;
            }
            int max = 1000; for (int i = 0; i < 7; i++) if (totals[i] > max) max = totals[i];
            float slot = width / 7f;
            for (int i = 0; i < 7; i++) {
                float barHeight = (height - dp(25)) * totals[i] / max;
                float left = i * slot + slot * 0.22f;
                float right = (i+1) * slot - slot * 0.22f;
                paint.setColor(i == 6 ? BLUE : Color.rgb(155,195,250));
                canvas.drawRoundRect(left, height - barHeight - dp(18), right, height - dp(18), dp(7), dp(7), paint);
                paint.setColor(mutedText); paint.setTextSize(dp(9));
                String label = new SimpleDateFormat("EE", Locale.US).format(new Date(System.currentTimeMillis() - (6-i)*86400000L));
                canvas.drawText(label, left, height - dp(3), paint);
            }
        }
    }
}
