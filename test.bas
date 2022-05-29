10 Let MAX = 5000
20 Let X = 1: Let Y = 1
30 If (X > MAX) GoTo 100
40 Print X
50 X = X + Y
60 If (Y > MAX) GoTo 100
70 Print Y
80 Y = X + Y
90 GoTo 30
100 End
110