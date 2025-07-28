import pandas as pd
import matplotlib.pyplot as plt

# Read the data
data = pd.read_csv('adaptation_data.csv')

# Create the plot
plt.figure(figsize=(10, 6))
plt.plot(data['TimeStep'], data['Accuracy'], marker='o', linestyle='-')

# Add titles and labels
plt.title('Adaptation to Pattern Change')
plt.xlabel('Time Step')
plt.ylabel('Prediction Accuracy (%)')
plt.grid(True)
plt.ylim(0, 105)
plt.xticks(range(0, len(data['TimeStep']) + 1, 2))

# Save the plot
plt.savefig('adaptation_curve.png')

print("Chart saved to adaptation_curve.png")
