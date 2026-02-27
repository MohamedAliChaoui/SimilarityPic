export interface ImageType {
  id: number;
  name: string;
  size: number;
  format: string;
  favorite: boolean; 
  hasDescriptors?: boolean;
  data?: string;
  username?: string;
  likes?: number;
  saves?: number;
  isLiked?: boolean;
  isSaved?: boolean;
  isPublished?: boolean;
}