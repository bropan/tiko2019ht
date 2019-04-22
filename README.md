# Database Programming 2019 Course Work 

##Setting things up
Connect to your shell, open psql and type:

```psql
ALTER_USER <omappt> WITH ENCRYPTED PASSWORD '<password>';
```

Create/Enter a suitable directory in your university shell

```bash
mkdir tiko
cd tiko
```

Clone this repository

```bash
git clone https://github.com/bropan/tiko2019ht.git
```

tiko2019ht directory will appear in this directory.
Switch there:

```bash
cd tiko2019ht
```

##Usage

To build (and download the driver if needed):

```bash
./build.sh
```

To run:

```bash
./run.sh
```

or do both:

```bash
./buildrun.sh
```

