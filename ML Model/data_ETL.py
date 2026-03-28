import pandas as pd
import numpy as np


def process_student_data_v2(file_path):
    """
    Extracts raw logs and engineers features specifically designed
    to feed the UI metrics (Mastery, Confidence, Gaps).
    """
    print("Loading and cleaning data...")
    df = pd.read_csv(file_path, low_memory=False)

    # 1. Essential columns for the UI and Model
    cols = ['studentId', 'skill', 'correct', 'action_num']
    # If your dataset has hint or time data, we can optionally grab it for the "Learning Path" context later
    if 'hintCount' in df.columns and 'timeTaken' in df.columns:
        cols.extend(['hintCount', 'timeTaken'])

    df = df[[c for c in cols if c in df.columns]].dropna(subset=['skill', 'correct'])
    df['correct'] = pd.to_numeric(df['correct'], errors='coerce')

    # Sort chronologically to prevent data leakage!
    if 'action_num' in df.columns:
        df = df.sort_values(by=['studentId', 'action_num'])

    print("Calculating rolling metrics for the dashboard...")

    # 2. Skill-Specific Metrics (Drives "Skills Gap" and "Strong Skills")
    df['skill_attempts'] = df.groupby(['studentId', 'skill']).cumcount()
    df['skill_cumulative_correct'] = df.groupby(['studentId', 'skill'])['correct'].cumsum() - df['correct']
    df['skill_accuracy'] = np.where(
        df['skill_attempts'] > 0,
        df['skill_cumulative_correct'] / df['skill_attempts'],
        0
    )

    # 3. Overall Student Metrics (Drives "Total Attempts" and "Avg Accuracy" on the UI)
    df['total_attempts'] = df.groupby('studentId').cumcount()
    df['total_cumulative_correct'] = df.groupby('studentId')['correct'].cumsum() - df['correct']
    df['overall_accuracy'] = np.where(
        df['total_attempts'] > 0,
        df['total_cumulative_correct'] / df['total_attempts'],
        0
    )

    # 4. Recent Trend Metric (Crucial for the "Performance Prediction" text)
    # How did they do on their last 3 questions?
    df['recent_3_correct'] = df.groupby('studentId')['correct'].transform(
        lambda x: x.shift().rolling(3, min_periods=1).sum())
    df['recent_accuracy'] = df['recent_3_correct'] / 3.0  # Approximate recent trend

    # Clean up calculation columns
    df = df.drop(columns=['skill_cumulative_correct', 'total_cumulative_correct', 'recent_3_correct'])

    print(f"Data ready! Shape: {df.shape}")
    return df

# Run it:
df_v2 = process_student_data_v2('anonymized_full_release_competition_dataset.csv')
print(df_v2.head())
df_v2.to_csv("cleaned2.csv")
df_v2.head().to_csv("cleaned_data_example.csv")