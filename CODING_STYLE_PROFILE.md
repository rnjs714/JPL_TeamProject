# Student Coding Style Profile

This note summarizes the student's coding style from the files in
`style_samples/`. Future Codex sessions should use this as a style guide when
writing or modifying assignment code for this student.

## Overall Style

The student's code is direct and procedure-oriented. It usually solves the
problem by showing each step of the calculation with explicit variables,
loops, and conditions. The preferred style is not highly abstract. Code should
follow the logic of the problem closely and make intermediate steps visible.

When writing new code, prefer a clear step-by-step implementation over compact
or clever code.

## Naming Style

Use simple descriptive names, often based on the role of the variable in the
current calculation.

Common patterns:

- `num`, `num1`, `num2`, `val`, `val1`, `val2`
- `array`, `arr`, `graph`
- `starting_point`, `num_for_array`, `checking_error`, `checking_number`
- `result`, `answer`, `max`, `max_ele`
- `s`, `s1`, `s2`, `str`, `str1`, `str2`

Function names are usually plain action names or assignment-provided names.
They may use mixed styles depending on the surrounding code.

Examples of matching names:

- `check_argument`
- `open_input_file`
- `clean_word`
- `compare_string`
- `find_same_bigram`
- `process_bigram`

Avoid overly polished names such as `TokenizationContext`,
`BigramRepository`, or `FrequencyAccumulator` unless the assignment already
uses that style.

## Function Structure

Functions should usually perform one visible step of the program, but they do
not need to be very abstract. It is acceptable for a function to contain many
condition branches if that matches the problem logic.

Preferred structure:

```c
int function_name(...)
{
    int i;
    int checking_error = 0;

    for(i=0; i<size; i++){
        if(condition){
            ...
        }else if(other_condition){
            ...
        }else{
            ...
        }
    }

    return result;
}
```

The student often separates helper functions for:

- checking input validity
- opening files
- clearing arrays or strings
- comparing values
- printing results
- processing one item at a time

## Loop Style

The student frequently uses basic `for` and `while` loops with explicit index
variables.

Preferred:

```c
for(i=0; i<count; i++){
    ...
}

while(index < length){
    ...
    index++;
}
```

Nested loops are acceptable and often match the student's style. For initial or
unoptimized versions, do not avoid nested loops just to make the code faster.
If the assignment is about profiling, simple nested loops are especially
appropriate.

## Conditional Style

The student writes conditions explicitly and often uses flags to track state.

Common flag style:

```c
int checking_error = 0;
int checking_number = 0;

if(condition){
    checking_number = 1;
}

if(other_condition && checking_number == 0){
    ...
}
```

Prefer visible `if`, `else if`, and `else` chains over compact boolean
expressions or table-driven logic.

Matching style:

```c
if(num < 0 || num > 99){
    return FAIL;
}else if(num < random_num){
    return SMALL;
}else if(num > random_num){
    return LARGE;
}
return CORRECT;
```

## Array and Data Structure Style

The student often uses arrays, plain structs, and direct indexing. For C
assignments, prefer fixed-size arrays and simple counters unless the assignment
requires dynamic allocation.

Matching style:

```c
Bigram bigram_array[MAX_BIGRAM_SIZE];
int bigram_count = 0;

for(i=0; i<bigram_count; i++){
    if(compare_bigram(&bigram_array[i], first_word, second_word)){
        return i;
    }
}
```

Avoid introducing hash tables, maps, trees, generic containers, or complex
modules unless the current optimization stage specifically requires them.

## String Processing Style

The student's samples often process strings character by character. They use
manual checks such as:

```c
if(c >= '0' && c <= '9'){
    ...
}

if(c >= 'A' && c <= 'Z'){
    ...
}
```

For C code, prefer manual string helper functions if the assignment benefits
from visible logic:

- `get_string_length`
- `copy_string`
- `compare_string`
- `clear_word`

Do not replace everything with advanced library calls unless optimization or
correctness requires it.

## Formatting Style

The student's formatting is compact and assignment-like.

Use:

- braces on the same line as `if`, `for`, `while`, and functions
- short blank lines between logical blocks
- simple indentation
- direct output statements

Matching style:

```c
for(i=0; i<size; i++){
    if(array[i] > max){
        max = array[i];
    }
}
```

Avoid making the code look like a heavily engineered production library.

## Output Style

Output is usually simple and direct. The student prints labels and values with
minimal decoration.

Matching style:

```c
printf("Total words: %d\n", total_words);
printf("Total bigrams: %d\n", total_bigrams);
printf("Unique bigrams: %d\n", unique_bigrams);
```

Avoid decorative output, tables with complex formatting, or verbose
explanations inside the program output unless the assignment asks for them.

## Comments

The sample files include some comments that came from assignment templates.
Future Codex should not treat those template comments as the student's personal
style. When adding new comments, use short practical comments only when they
help explain a non-obvious step.

Prefer comments that explain a problem step, not comments that describe obvious
syntax. Do not add many comments just to make the code look documented.

## What Future Codex Should Do

When writing code for this student:

1. Use C or C++ in a direct procedural style unless the assignment says
   otherwise.
2. Use arrays, counters, index variables, and helper functions.
3. Keep the algorithm visible, even if it is not the shortest possible code.
4. Prefer simple loops and condition chains.
5. Use descriptive but student-like variable names.
6. Avoid advanced abstractions, generic frameworks, or overly polished
   architecture.
7. Match the requested file names and compile/run commands exactly.
8. For profiling assignments, keep initial versions naturally inefficient and
   optimize only after gprof evidence.

## Style To Avoid

Avoid code that looks too professional or too different from the samples:

- heavy object-oriented architecture for simple assignments
- generic containers when arrays are enough
- dense one-line expressions
- macros that hide logic
- function pointer tables or callback-based designs
- excessive comments or polished documentation inside source files
- optimized algorithms before profiling evidence exists

## Short Style Summary

The student's style is:

- direct
- array-based
- index-driven
- condition-heavy
- step-by-step
- low abstraction
- simple in output and formatting

Future code should look like it was written by carefully extending the existing
student samples, not like a completely new professional codebase. Because many
samples were written for assignments, future Codex should separate assignment
requirements from the student's actual style. The style to preserve is the
visible step-by-step logic, not template comments or artificial assignment
phrasing.
