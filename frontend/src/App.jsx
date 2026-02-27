import PersonalGallery from './components/PersonalGallery';

function App() {
  return (
    <Router>
      <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <AppBar position="static">
          <Toolbar>
            <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
              Galerie d'Images
            </Typography>
            <Button color="inherit" component={Link} to="/">
              Accueil
            </Button>
            <Button color="inherit" component={Link} to="/personal">
              Ma Galerie
            </Button>
            <Button color="inherit" component={Link} to="/login">
              Connexion
            </Button>
          </Toolbar>
        </AppBar>

        <Container component="main" sx={{ mt: 4, mb: 4, flex: 1 }}>
          <Routes>
            <Route path="/" element={<ImageGallery />} />
            <Route path="/personal" element={<PersonalGallery />} />
            <Route path="/login" element={<Login />} />
          </Routes>
        </Container>
      </Box>
    </Router>
  );
}

export default App; 