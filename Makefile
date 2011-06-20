clean : 
	rm -rf data/treebank

download-treebank : clean
	cd data && \
	wget http://nltk.googlecode.com/svn/trunk/nltk_data/packages/corpora/treebank.zip && \
	unzip treebank.zip
	rm data/treebank.zip

train-parser : download-treebank
	lein run -m es.corygil.nlptk.parser
