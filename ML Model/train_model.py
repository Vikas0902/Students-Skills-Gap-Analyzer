import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, roc_auc_score, classification_report
import joblib

def train_evaluate_and_export(csv_file_path, export_path='skill_gap_model.joblib'):
    print(f"Loading data from {csv_file_path}...")
    df = pd.read_csv(csv_file_path)

    if 'recent_accuracy' in df.columns and 'overall_accuracy' in df.columns:
        df['recent_accuracy'] = df['recent_accuracy'].fillna(df['overall_accuracy'])

    features = [
        'skill_attempts', 'skill_accuracy',
        'total_attempts', 'overall_accuracy', 'recent_accuracy'
    ]
    target = 'correct'

    df_clean = df.dropna(subset=features + [target])
    X = df_clean[features]
    y = df_clean[target]

    # --- NEW: Split the data for evaluation ---
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, shuffle=False)

    print(f"Training Logistic Regression model...")
    model = LogisticRegression(max_iter=1000, class_weight='balanced')
    model.fit(X_train, y_train)

    # --- NEW: Evaluate the model ---
    print("\nEvaluating Model Performance...")
    predictions = model.predict(X_test)
    probabilities = model.predict_proba(X_test)[:, 1]

    print(f"Accuracy: {accuracy_score(y_test, predictions):.4f}")
    print(f"ROC-AUC:  {roc_auc_score(y_test, probabilities):.4f}")
    print("\nClassification Report:")
    print(classification_report(y_test, predictions))

    # --- Export the Model ---
    # Note: We save the model trained on the 80% split here. For a final production
    # run, you might want to retrain on 100% of the data (X, y) before saving.
    joblib.dump(model, export_path)
    print(f"\nSuccess! Model exported to: {export_path}")

if __name__ == "__main__":
    dataset_path = 'cleaned2.csv' # Make sure this points to your file
    train_evaluate_and_export(dataset_path)