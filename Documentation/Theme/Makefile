.PHONY: all
all:
	./latexrun Theme.tex
	mv Theme.pdf Entwurf\ Bachelorthesis\ -\ Tobias\ Kerst.pdf

.PHONY: clean
clean:
	./latexrun --clean-all && rm -rf latex.out && rm *.pdf
