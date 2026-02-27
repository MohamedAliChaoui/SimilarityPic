import React, { useState, useEffect } from 'react';
import { Box, Typography, Grid, IconButton, Card, CardMedia, CardContent, CardActions, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { Favorite, FavoriteBorder } from '@mui/icons-material';
import axios from 'axios';

const ImageGallery = () => {
  const [images, setImages] = useState([]);
  const [selectedImage, setSelectedImage] = useState(null);
  const [openDialog, setOpenDialog] = useState(false);
  const token = localStorage.getItem('token');

  useEffect(() => {
    fetchImages();
  }, []);

  const fetchImages = async () => {
    try {
      const response = await axios.get('http://localhost:8009/images/feed');
      setImages(response.data);
    } catch (error) {
      console.error('Erreur lors de la récupération des images:', error);
    }
  };

  const handleImageClick = (image) => {
    setSelectedImage(image);
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedImage(null);
  };

  const toggleFavorite = async (imageId) => {
    if (!token) {
      alert('Veuillez vous connecter pour ajouter aux favoris');
      return;
    }
    try {
      await axios.put(`http://localhost:8009/images/${imageId}/favorite?token=${token}`);
      fetchImages();
    } catch (error) {
      console.error('Erreur lors de la modification du favori:', error);
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Galerie Publique
      </Typography>
      <Grid container spacing={3}>
        {images.map((image) => (
          <Grid item xs={12} sm={6} md={4} key={image.id}>
            <Card>
              <CardMedia
                component="img"
                height="200"
                image={`http://localhost:8009/images/${image.id}/data`}
                alt={image.name}
                onClick={() => handleImageClick(image)}
                sx={{ cursor: 'pointer' }}
              />
              <CardContent>
                <Typography variant="h6" noWrap>
                  {image.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {image.format} - {(image.size / 1024).toFixed(2)} KB
                </Typography>
                {image.username && (
                  <Typography variant="body2" color="text.secondary">
                    Publié par: {image.username}
                  </Typography>
                )}
              </CardContent>
              <CardActions>
                <IconButton onClick={() => toggleFavorite(image.id)}>
                  {image.favorite ? <Favorite color="error" /> : <FavoriteBorder />}
                </IconButton>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        {selectedImage && (
          <>
            <DialogTitle>{selectedImage.name}</DialogTitle>
            <DialogContent>
              <img
                src={`http://localhost:8009/images/${selectedImage.id}/data`}
                alt={selectedImage.name}
                style={{ width: '100%', height: 'auto' }}
              />
            </DialogContent>
            <DialogActions>
              <Button onClick={handleCloseDialog}>Fermer</Button>
            </DialogActions>
          </>
        )}
      </Dialog>
    </Box>
  );
};

export default ImageGallery; 