# Course Prerequisite Grapher

A Java-based utility that parses course data to generate visual Directed Acyclic Graphs (DAGs) using Mermaid.js.
It dynamically generates visual flowcharts to help students and advisors visualize complex degree paths.

## Features
- **Cycle Detection:** Automatically identifies and prevents circular prerequisites.
- **Dynamic Styling:** Automatically colors nodes by course prefix.
- **Major Identification:** Applies distinct styling to "Major" requirement nodes.
- **In-Memory Validation:** High-speed graph validation logic.

## Usage

### 1. Prepare your Data
The program processes data from CSV files. Ensure your files follow this format:

*Example:*
```text
Prerequisite,Course
TMATH 124,TMATH 125
TMATH 125,TMATH 208
TCSS 142,TCSS 143
```

### 2. Run the Program
1. Open the project in IntelliJ IDEA.
2. Place your CSV file in the root directory of the project. 
3. Run `Main.java`.
4. When prompted in the console, type the name of your CSV file and press Enter.

**Project Structure Example:**
```text
CourseGrapher/
├── src/
├── your_file.csv  <-- (Place files here!)
├── graph.txt
└── README.md
```

### 3. View the Output
A file named `graph.txt` will be generated containing the **Mermaid** syntax. 
Copy the contents of `graph.txt` into the [Mermaid Live Editor](https://mermaid.live/) to see the visual graph!

## Example Output
Below is a visualization of the current prerequisite model for the CSS Major:

```mermaid
---
title: Course Prerequisite Model using DAG
---
graph TD
    TCSS_305["TCSS 305"] --> TCSS_342["TCSS 342"]
    TCSS_321["TCSS 321"] --> TCSS_342["TCSS 342"]
    TCSS_342["TCSS 342"] --> TCSS_343["TCSS 343"]
    TCSS_342["TCSS 342"] --> TCSS_360["TCSS 360"]
    TCSS_342["TCSS 342"] --> TCSS_380["TCSS 380"]
    CSS_Major["CSS Major"] --> TCSS_305["TCSS 305"]
    CSS_Major["CSS Major"] --> TCSS_321["TCSS 321"]
    CSS_Major["CSS Major"] --> TCSS_325["TCSS 325"]
    CSS_Major["CSS Major"] --> TCSS_371["TCSS 371"]
    Lab_Science["Lab Science"] --> CSS_Major["CSS Major"]
    Math_Placement["Math Placement"] --> TMATH_124["TMATH 124"]
    Math_Placement["Math Placement"] --> TCSS_142["TCSS 142"]
    TMATH_124["TMATH 124"] --> TMATH_125["TMATH 125"]
    TMATH_124["TMATH 124"] --> CSS_Major["CSS Major"]
    TMATH_125["TMATH 125"] --> TMATH_208["TMATH 208"]
    TMATH_125["TMATH 125"] --> TMATH_126["TMATH 126"]
    TMATH_125["TMATH 125"] --> CSS_Major["CSS Major"]
    TMATH_126["TMATH 126"] --> TMATH_390["TMATH 390"]
    TCSS_143["TCSS 143"] --> CSS_Major["CSS Major"]
    TCSS_142["TCSS 142"] --> TCSS_143["TCSS 143"]
    TCSS_142["TCSS 142"] --> CSS_Major["CSS Major"]
    TCSS_372["TCSS 372"] --> TCSS_422["TCSS 422"]
    TCSS_371["TCSS 371"] --> TCSS_372["TCSS 372"]
    TCSS_371["TCSS 371"] --> TCSS_380["TCSS 380"]
    None["None"] --> Lab_Science["Lab Science"]
    TCSS_380["TCSS 380"] --> TCSS_422["TCSS 422"]

    %% Dynamic Styling
    classDef styleTCSS fill:#e1f5fe,stroke:#01579b,stroke-width:2px;
    class TCSS_305 styleTCSS
    class TCSS_325 styleTCSS
    class TCSS_422 styleTCSS
    classDef styleTMATH fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px;
    class TMATH_390 styleTMATH
    class TCSS_321 styleTCSS
    class TCSS_343 styleTCSS
    class TCSS_342 styleTCSS
    classDef styleCSS fill:#fff3e0,stroke:#e65100,stroke-width:2px;
    class CSS_Major styleCSS
    classDef styleLab fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px;
    class Lab_Science styleLab
    classDef styleMath fill:#f1f8e9,stroke:#558b2f,stroke-width:2px;
    class Math_Placement styleMath
    class TMATH_124 styleTMATH
    class TMATH_125 styleTMATH
    class TMATH_126 styleTMATH
    class TMATH_208 styleTMATH
    class TCSS_143 styleTCSS
    class TCSS_142 styleTCSS
    class TCSS_372 styleTCSS
    class TCSS_371 styleTCSS
    class TCSS_360 styleTCSS
    class TCSS_380 styleTCSS

    classDef majorNode fill:#fff,stroke:#333,stroke-width:4px,stroke-dasharray: 5 5;
    class CSS_Major majorNode
