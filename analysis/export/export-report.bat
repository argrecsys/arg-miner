cd ".."
call conda activate base
jupyter nbconvert DataAnalysis.ipynb --no-input --to html
jupyter nbconvert NetworkAnalysis.ipynb --no-input --to html
