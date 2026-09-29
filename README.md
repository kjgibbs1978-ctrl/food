# Keith Food Tracker V1.0

A deliberately simple standalone Android food tracker for the fixed diet plan.

## V1 features

- Completely separate app/package from Gym Progress Tracker.
- Works offline and stores data locally on the phone.
- Fixed strict-diet food list only.
- Select a food, use its normal serving, or edit the amount before logging.
- Coffee pods are logged one at a time.
- Coffee milk defaults to 100 ml and is logged separately from the pod.
- Live daily totals: calories, protein, carbs, fat and fibre.
- Macro donut chart based on macro calories (4/4/9 rule).
- Today's individual log entries can be edited or deleted.
- Daily history with tap-to-view food detail.
- 7-day logged-day calorie/protein averages and protein-target hit count.

## Checked reference day

The built-in standard portions reproduce:

- 1,886 kcal
- 168.7 g protein
- 205.5 g carbohydrate
- 41.3 g fat
- 38.2 g fibre

No foods are assumed as eaten. Totals only include food actually logged.

## Build in GitHub Codespaces

From the project root:

```bash
bash build.sh
```

The APK is created at:

`app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions

The included workflow builds the APK automatically on pushes to `main`, or it can be started manually under **Actions > Build Food Tracker APK > Run workflow**.
